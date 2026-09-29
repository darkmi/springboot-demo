package com.darkmi.springboot.emaillogin.service;

import com.darkmi.springboot.emaillogin.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱验证码服务：生成、存储、频控、校验。
 *
 * <p>与短信验证码的差别只在"发送渠道"，生成、存储、频控、校验逻辑完全一致。
 * 示例将验证码存储在内存 Map 中；生产环境请替换为 Redis，见文章"生产环境建议"一节。</p>
 */
@Service
public class EmailCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** key = 邮箱地址，value = 验证码上下文 */
    private final Map<String, CodeEntry> cache = new ConcurrentHashMap<>();

    private final MailSendService mailSendService;

    /** 验证码有效期（分钟） */
    @Value("${email.code.expire-minutes:10}")
    private long expireMinutes;

    /** 同一邮箱两次发送的最小间隔（秒） */
    @Value("${email.code.resend-interval-seconds:60}")
    private long resendIntervalSeconds;

    /** 单个验证码允许的最大错误次数 */
    @Value("${email.code.max-attempts:5}")
    private int maxAttempts;

    public EmailCodeService(MailSendService mailSendService) {
        this.mailSendService = mailSendService;
    }

    /**
     * 发送验证码：频控 → 生成 6 位数字码 → 存入缓存 → 发送邮件
     */
    public Result<Void> sendCode(String email) {
        long now = System.currentTimeMillis();

        // 1. 发送频控：60 秒内不允许重复发送，防邮件轰炸
        CodeEntry old = cache.get(email);
        if (old != null && now < old.nextSendAtMillis()) {
            long waitSeconds = (old.nextSendAtMillis() - now) / 1000 + 1;
            return Result.error("发送过于频繁，请 " + waitSeconds + " 秒后再试");
        }

        // 2. 生成 6 位数字验证码：SecureRandom 强随机源，%06d 补齐前导零
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        // 3. 存入缓存并记录有效期、下次可发送时间、剩余错误次数
        cache.put(email, new CodeEntry(
                code,
                now + expireMinutes * 60 * 1000,
                now + resendIntervalSeconds * 1000,
                maxAttempts));

        // 4. 发送邮件（SMTP 交互可能耗时数秒，生产环境建议异步化）
        mailSendService.sendCaptchaMail(email, code);
        return Result.success("验证码已发送，" + expireMinutes + " 分钟内有效");
    }

    /**
     * 校验验证码：过期校验 → 错误次数校验 → 比对 → 一次性消费
     */
    public Result<Void> verifyCode(String email, String code) {
        long now = System.currentTimeMillis();

        CodeEntry entry = cache.get(email);
        if (entry == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        // 有效期校验：过期立即清除，防止"过期码"在缓存清理前仍可用
        if (now > entry.expireAtMillis()) {
            cache.remove(email);
            return Result.error("验证码已过期，请重新获取");
        }
        // 错误次数校验：连续错 5 次作废，防暴力试错
        if (entry.remainAttempts() <= 0) {
            cache.remove(email);
            return Result.error("错误次数过多，验证码已失效，请重新获取");
        }
        // 比对失败：剩余错误次数减一
        if (!entry.code().equals(code)) {
            cache.put(email, entry.withRemainAttempts(entry.remainAttempts() - 1));
            return Result.error("验证码错误，还剩 " + (entry.remainAttempts() - 1) + " 次机会");
        }
        // 校验通过：一次性消费，无论何时都不能用同一个验证码登录两次
        cache.remove(email);
        return Result.success("验证码校验通过");
    }

    /**
     * 验证码上下文：码本身 + 过期时间 + 下次可发送时间 + 剩余错误次数
     */
    private record CodeEntry(String code, long expireAtMillis, long nextSendAtMillis, int remainAttempts) {

        private CodeEntry withRemainAttempts(int newRemainAttempts) {
            return new CodeEntry(code, expireAtMillis, nextSendAtMillis, newRemainAttempts);
        }
    }
}

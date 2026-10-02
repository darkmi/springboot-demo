package com.darkmi.springboot.smslogin.service;

import com.darkmi.springboot.smslogin.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务：生成、存储、频控、校验。
 *
 * <p>示例将验证码存储在内存 Map 中（单机可用）；生产环境请替换为 Redis，
 * 并将"发送频控 / 有效期 / 错误次数上限"三个参数一并迁移，见文章"生产环境建议"一节。</p>
 */
@Service
public class SmsCodeService {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeService.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    /** key = 手机号，value = 验证码上下文 */
    private final Map<String, CodeEntry> cache = new ConcurrentHashMap<>();

    private final SmsSender smsSender;

    /** 验证码有效期（分钟） */
    @Value("${sms.code.expire-minutes:5}")
    private long expireMinutes;

    /** 同一手机号两次发送的最小间隔（秒） */
    @Value("${sms.code.resend-interval-seconds:60}")
    private long resendIntervalSeconds;

    /** 单个验证码允许的最大错误次数 */
    @Value("${sms.code.max-attempts:5}")
    private int maxAttempts;

    public SmsCodeService(SmsSender smsSender) {
        this.smsSender = smsSender;
    }

    /**
     * 发送验证码：频控 → 生成 6 位数字码 → 存入缓存 → 调用短信服务商发送
     */
    public Result<Void> sendCode(String mobile) {
        long now = System.currentTimeMillis();

        // 1. 发送频控：60 秒内不允许重复发送，防短信轰炸
        CodeEntry old = cache.get(mobile);
        if (old != null && now < old.nextSendAtMillis()) {
            long waitSeconds = (old.nextSendAtMillis() - now) / 1000 + 1;
            return Result.error("发送过于频繁，请 " + waitSeconds + " 秒后再试");
        }

        // 2. 生成 6 位数字验证码：SecureRandom 强随机源，%06d 补齐前导零
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        // 3. 存入缓存并记录有效期、下次可发送时间、剩余错误次数
        cache.put(mobile, new CodeEntry(
                code,
                now + expireMinutes * 60 * 1000,
                now + resendIntervalSeconds * 1000,
                maxAttempts));

        // 4. 调用短信服务商发送：失败时回滚频控记录，让用户可以立即重试
        try {
            smsSender.send(mobile, code);
        } catch (Exception e) {
            log.error("短信发送失败 mobile={}：{}", mobile, e.getMessage(), e);
            cache.remove(mobile);
            return Result.error("短信发送失败，请稍后重试");
        }
        return Result.success("验证码已发送，" + expireMinutes + " 分钟内有效");
    }

    /**
     * 校验验证码：过期校验 → 错误次数校验 → 比对 → 一次性消费
     */
    public Result<Void> verifyCode(String mobile, String code) {
        long now = System.currentTimeMillis();

        CodeEntry entry = cache.get(mobile);
        if (entry == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        // 有效期校验：过期立即清除，防止"过期码"在缓存清理前仍可用
        if (now > entry.expireAtMillis()) {
            cache.remove(mobile);
            return Result.error("验证码已过期，请重新获取");
        }
        // 错误次数校验：连续错 5 次作废，防暴力试错
        if (entry.remainAttempts() <= 0) {
            cache.remove(mobile);
            return Result.error("错误次数过多，验证码已失效，请重新获取");
        }
        // 比对失败：剩余错误次数减一
        if (!entry.code().equals(code)) {
            cache.put(mobile, entry.withRemainAttempts(entry.remainAttempts() - 1));
            return Result.error("验证码错误，还剩 " + (entry.remainAttempts() - 1) + " 次机会");
        }
        // 校验通过：一次性消费，无论何时都不能用同一个验证码登录两次
        cache.remove(mobile);
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

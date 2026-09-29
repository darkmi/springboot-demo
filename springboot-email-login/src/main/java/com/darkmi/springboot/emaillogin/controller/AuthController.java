package com.darkmi.springboot.emaillogin.controller;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import com.darkmi.springboot.emaillogin.common.Result;
import com.darkmi.springboot.emaillogin.service.EmailCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 邮箱验证码登录接口。
 *
 * <p>示例未接入用户表：验证码校验通过即视为登录成功；生产环境中此处应替换为
 * "按邮箱查询用户 → 未注册时的处理策略（拒绝或自动注册）→ 签发会话/Token"。</p>
 */
@RestController
@RequestMapping("/auth/email")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final EmailCodeService emailCodeService;

    public AuthController(EmailCodeService emailCodeService) {
        this.emailCodeService = emailCodeService;
    }

    public record SendCodeReq(String email) {
    }

    public record EmailLoginReq(String email, String code) {
    }

    /** 发送邮箱验证码：POST /auth/email/send，body: {"email":"user@example.com"} */
    @PostMapping("/send")
    public Result<Void> send(@RequestBody SendCodeReq req) {
        if (!Validator.isEmail(req.email())) {
            return Result.error("邮箱格式不正确");
        }
        try {
            return emailCodeService.sendCode(req.email());
        } catch (Exception e) {
            // SMTP 连接失败、认证失败、对方拒信等都汇聚为友好提示，原始异常记入日志
            log.error("验证码邮件发送失败：{}", req.email(), e);
            return Result.error("邮件发送失败，请稍后重试");
        }
    }

    /** 验证码登录：POST /auth/email/login，body: {"email":"user@example.com","code":"123456"} */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody EmailLoginReq req) {
        if (!Validator.isEmail(req.email())) {
            return Result.error("邮箱格式不正确");
        }
        Result<Void> verifyResult = emailCodeService.verifyCode(req.email(), req.code());
        if (verifyResult.code() != 0) {
            return Result.error(verifyResult.msg());
        }
        // 验证码通过 → 示例直接签发模拟 Token；生产环境在此查询用户并创建会话
        String token = IdUtil.fastSimpleUUID();
        return Result.success("登录成功", Map.of("token", token));
    }
}

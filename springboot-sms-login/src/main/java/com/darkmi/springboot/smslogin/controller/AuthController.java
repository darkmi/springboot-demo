package com.darkmi.springboot.smslogin.controller;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import com.darkmi.springboot.smslogin.common.Result;
import com.darkmi.springboot.smslogin.service.SmsCodeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 短信验证码登录接口。
 *
 * <p>示例未接入用户表：验证码校验通过即视为登录成功；生产环境中此处应替换为
 * "按手机号查询用户 → 未注册时的处理策略（拒绝或自动注册）→ 签发会话/Token"。</p>
 */
@RestController
@RequestMapping("/auth/sms")
public class AuthController {

    private final SmsCodeService smsCodeService;

    public AuthController(SmsCodeService smsCodeService) {
        this.smsCodeService = smsCodeService;
    }

    public record SendCodeReq(String mobile) {
    }

    public record SmsLoginReq(String mobile, String code) {
    }

    /** 发送短信验证码：POST /auth/sms/send，body: {"mobile":"13800138000"} */
    @PostMapping("/send")
    public Result<Void> send(@RequestBody SendCodeReq req) {
        if (!Validator.isMobile(req.mobile())) {
            return Result.error("手机号格式不正确");
        }
        return smsCodeService.sendCode(req.mobile());
    }

    /** 验证码登录：POST /auth/sms/login，body: {"mobile":"13800138000","code":"123456"} */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody SmsLoginReq req) {
        if (!Validator.isMobile(req.mobile())) {
            return Result.error("手机号格式不正确");
        }
        Result<Void> verifyResult = smsCodeService.verifyCode(req.mobile(), req.code());
        if (verifyResult.code() != 0) {
            return Result.error(verifyResult.msg());
        }
        // 验证码通过 → 示例直接签发模拟 Token；生产环境在此查询用户并创建会话
        String token = IdUtil.fastSimpleUUID();
        return Result.success("登录成功", Map.of("token", token));
    }
}

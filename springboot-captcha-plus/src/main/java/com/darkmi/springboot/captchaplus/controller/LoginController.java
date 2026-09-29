package com.darkmi.springboot.captchaplus.controller;

import com.xingyuv.captcha.model.common.ResponseModel;
import com.xingyuv.captcha.model.vo.CaptchaVO;
import com.xingyuv.captcha.service.CaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录示例：演示验证码通过后的服务端"二次校验"，
 * 防止攻击者绕过前端行为验证直接调用登录接口。
 *
 * <p>与芋道源码 AdminAuthServiceImpl#validateCaptcha 的处理方式一致：
 * 登录入口先做验证码二次校验，通过后才执行真正的认证逻辑，
 * 并提供 captcha.enable 开关，便于开发/压测环境一键关闭验证码。</p>
 */
@RestController
@RequestMapping("/auth")
public class LoginController {

    private final CaptchaService captchaService;

    /** 验证码开关：生产环境保持开启，开发/压测时可配置为 false 一键关闭 */
    @Value("${captcha.enable:true}")
    private boolean captchaEnable;

    public LoginController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 登录接口：前端 check 通过后携带 captchaVerification（加密令牌）请求本接口做二次校验
     */
    @PostMapping("/login")
    public ResponseModel login(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String captchaVerification) {
        // 0. 开关关闭时跳过验证码校验（芋道源码：captchaEnable 为 false 直接放行）
        if (captchaEnable) {
            // 1. 二次校验验证码（令牌一次性消费）
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaVerification(captchaVerification);
            ResponseModel response = captchaService.verification(captchaVO);
            if (!response.isSuccess()) {
                // repCode: 0000 成功 / 0011 参数不能为空 / 6110 验证码已失效 / 6111 验证失败 ...
                return response;
            }
        }

        // 2. 验证码校验通过，执行真正的登录逻辑（示例中任意账号密码均可登录）
        return ResponseModel.successMsg("登录成功，欢迎 " + username);
    }
}

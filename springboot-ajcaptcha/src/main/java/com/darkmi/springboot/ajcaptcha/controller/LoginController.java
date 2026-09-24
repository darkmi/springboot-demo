package com.darkmi.springboot.ajcaptcha.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录示例：演示验证码通过后的服务端"二次校验"，
 * 防止攻击者绕过前端行为验证直接调用登录接口。
 */
@RestController
@RequestMapping("/auth")
public class LoginController {

	private final CaptchaService captchaService;

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
		// 1. 二次校验验证码（令牌一次性消费）
		CaptchaVO captchaVO = new CaptchaVO();
		captchaVO.setCaptchaVerification(captchaVerification);
		ResponseModel response = captchaService.verification(captchaVO);
		if (!response.isSuccess()) {
			// repCode: 0000 成功 / 0011 参数不能为空 / 6110 验证码已失效 / 6111 验证失败 ...
			return response;
		}

		// 2. 验证码校验通过，执行真正的登录逻辑（示例中任意账号密码均可登录）
		return ResponseModel.successMsg("登录成功，欢迎 " + username);
	}
}

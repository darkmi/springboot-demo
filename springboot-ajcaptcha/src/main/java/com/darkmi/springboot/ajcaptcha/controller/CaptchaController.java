package com.darkmi.springboot.ajcaptcha.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行为验证码接口：获取验证码 / 前端核对。
 *
 * <p>官方 starter 内置的 CaptchaController 基于 javax.servlet 无法在 Spring Boot 4 运行，
 * 这里按官方实现改写为 jakarta 命名空间，接口路径保持一致（/captcha/get、/captcha/check），
 * 与官方前端 verify.js 插件直接对接。</p>
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

	private final CaptchaService captchaService;

	public CaptchaController(CaptchaService captchaService) {
		this.captchaService = captchaService;
	}

	/** 获取验证码（滑动拼图底图 / 文字点选底图，Base64 编码返回） */
	@PostMapping("/get")
	public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
		data.setBrowserInfo(getRemoteId(request));
		return captchaService.get(data);
	}

	/** 前端滑动/点选完成后的核对接口，通过后返回二次校验令牌 captchaVerification */
	@PostMapping("/check")
	public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request) {
		data.setBrowserInfo(getRemoteId(request));
		return captchaService.check(data);
	}

	/** 以 客户端IP + User-Agent 作为浏览器标识，参与 AES 加密与接口频控 */
	public static String getRemoteId(HttpServletRequest request) {
		String xfwd = request.getHeader("X-Forwarded-For");
		String ip = getRemoteIpFromXfwd(xfwd);
		String ua = request.getHeader("user-agent");
		if (StringUtils.isNotBlank(ip)) {
			return ip + ua;
		}
		return request.getRemoteAddr() + ua;
	}

	private static String getRemoteIpFromXfwd(String xfwd) {
		if (StringUtils.isNotBlank(xfwd)) {
			String[] ipList = xfwd.split(",");
			return StringUtils.trim(ipList[0]);
		}
		return null;
	}
}

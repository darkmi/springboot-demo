package com.darkmi.springbootdemo.captcha.controller;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.GifCaptcha;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

	/** 验证码文本在 Session 中的 key */
	public static final String CAPTCHA_SESSION_KEY = "CAPTCHA_CODE";

	/**
	 * 生成验证码并输出图片流
	 * 路径示例：/captcha/line、/captcha/circle、/captcha/shear、/captcha/gif
	 */
	@GetMapping("/{type}")
	public void captcha(@PathVariable String type,
						HttpServletRequest request,
						HttpServletResponse response) throws IOException {
		AbstractCaptcha captcha = switch (type) {
			case "line"   -> CaptchaUtil.createLineCaptcha(200, 100, 4, 20);   // 4 位字符，20 条干扰线
			case "circle" -> CaptchaUtil.createCircleCaptcha(200, 100, 4, 10); // 4 位字符，10 个干扰圆
			case "shear"  -> CaptchaUtil.createShearCaptcha(200, 100, 4, 4);   // 4 位字符，扭曲度 4
			case "gif"    -> CaptchaUtil.createGifCaptcha(200, 100, 4);        // 4 位字符，动态帧
			default -> throw new IllegalArgumentException("不支持的验证码类型：" + type);
		};

		// 1. 取出验证码文本存入 Session，供后续校验
		request.getSession().setAttribute(CAPTCHA_SESSION_KEY, captcha.getCode());

		// 2. GIF 为动图格式，其余三种为 PNG
		response.setContentType(captcha instanceof GifCaptcha ? "image/gif" : "image/png");
		response.setHeader("Cache-Control", "no-store, no-cache");

		// 3. 输出图片字节流
		captcha.write(response.getOutputStream());
	}

	/**
	 * 校验验证码：GET /captcha/verify?code=用户输入
	 */
	@GetMapping("/verify")
	public String verify(@RequestParam String code, HttpServletRequest request) {
		String expected = (String) request.getSession().getAttribute(CAPTCHA_SESSION_KEY);
		// 一次性消费：无论成功与否都移除，防止同一个验证码被反复试错
		request.getSession().removeAttribute(CAPTCHA_SESSION_KEY);

		return StrUtil.isNotBlank(expected) && StrUtil.equalsIgnoreCase(expected, code)
				? "验证通过" : "验证码错误或已过期";
	}
}

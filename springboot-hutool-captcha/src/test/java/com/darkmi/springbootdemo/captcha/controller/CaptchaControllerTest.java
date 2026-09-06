package com.darkmi.springbootdemo.captcha.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CaptchaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void lineCaptchaReturnsPng() throws Exception {
		mockMvc.perform(get("/captcha/line"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("image/png"))
				.andExpect(header().string("Cache-Control", "no-store, no-cache"));
	}

	@Test
	void gifCaptchaReturnsGif() throws Exception {
		mockMvc.perform(get("/captcha/gif"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("image/gif"));
	}

	@Test
	void verifySucceedsWithRightCodeAndConsumesItOnce() throws Exception {
		// 1. 生成验证码，从 Session 中取出验证码文本（模拟"看图输入"）
		MvcResult result = mockMvc.perform(get("/captcha/circle")).andExpect(status().isOk()).andReturn();
		MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
		String code = (String) session.getAttribute(CaptchaController.CAPTCHA_SESSION_KEY);

		// 2. 输入正确验证码 → 校验通过（忽略大小写）
		mockMvc.perform(get("/captcha/verify").param("code", code.toLowerCase()).session(session))
				.andExpect(content().string("验证通过"));

		// 3. 同一验证码二次校验 → 因一次性消费而失败
		mockMvc.perform(get("/captcha/verify").param("code", code).session(session))
				.andExpect(content().string("验证码错误或已过期"));
	}

	@Test
	void verifyFailsWithWrongCode() throws Exception {
		MvcResult result = mockMvc.perform(get("/captcha/shear")).andExpect(status().isOk()).andReturn();
		MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

		mockMvc.perform(get("/captcha/verify").param("code", "wrong-code").session(session))
				.andExpect(content().string("验证码错误或已过期"));
	}
}

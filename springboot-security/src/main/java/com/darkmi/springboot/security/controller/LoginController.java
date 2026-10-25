package com.darkmi.springboot.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 登录页。POST /login 由 Spring Security 的 UsernamePasswordAuthenticationFilter 处理，
 * 无需自定义登录接口；失败时框架重定向 /login?error，模板据此显示错误提示。
 */
@Controller
public class LoginController {

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

}

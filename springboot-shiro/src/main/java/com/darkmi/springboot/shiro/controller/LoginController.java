package com.darkmi.springboot.shiro.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录页与登录动作。/login 在过滤器链中放行（anon），登录在此手动调用 Subject.login。
 */
@Controller
public class LoginController {

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@PostMapping("/login")
	public String login(@RequestParam String username,
			@RequestParam String password,
			Model model) {
		Subject subject = SecurityUtils.getSubject();
		try {
			subject.login(new UsernamePasswordToken(username, password));
		} catch (AuthenticationException e) {
			// 凭据错误、账户锁定等统一提示，不暴露具体原因
			model.addAttribute("loginError", "用户名或密码错误");
			model.addAttribute("username", username);
			return "login";
		}
		return "redirect:/";
	}

}

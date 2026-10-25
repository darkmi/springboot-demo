package com.darkmi.springboot.security.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.darkmi.springboot.security.service.UserService;

/**
 * 三个菜单页面：个人中心（登录即可）、用户管理（user:list）、订单管理（order:list）。
 * 权限拦截在 SecurityConfig 的 URL 规则中完成，越权由框架重定向 /403，无需注解。
 */
@Controller
public class PageController {

	private final UserService userService;

	public PageController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 个人中心：展示当前登录用户信息。
	 */
	@GetMapping("/")
	public String profile(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth.getName();
		model.addAttribute("user", userService.findByUsername(username));
		model.addAttribute("roles", String.join(", ", userService.findRoles(username)));
		return "profile";
	}

	/**
	 * 用户管理：仅 user_admin 角色（用户 a）可访问。
	 */
	@GetMapping("/users")
	public String users(Model model) {
		model.addAttribute("users", userService.listUsers());
		return "users";
	}

	/**
	 * 订单管理：仅 order_admin 角色（用户 b）可访问。
	 */
	@GetMapping("/orders")
	public String orders(Model model) {
		model.addAttribute("orders", userService.listOrders());
		return "orders";
	}

	/**
	 * 越权提示页（由 accessDeniedUrl 跳转过来）。
	 */
	@GetMapping("/403")
	public String forbidden() {
		return "403";
	}

}

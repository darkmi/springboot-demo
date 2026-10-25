package com.darkmi.springboot.shiro.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.darkmi.springboot.shiro.service.UserService;

/**
 * 三个菜单页面：个人中心（登录即可）、用户管理（user:list）、订单管理（order:list）。
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
		Subject subject = SecurityUtils.getSubject();
		String username = (String) subject.getPrincipal();
		model.addAttribute("user", userService.findByUsername(username));
		model.addAttribute("roles", String.join(", ", userService.findRoles(username)));
		return "profile";
	}

	/**
	 * 用户管理：仅 user_admin 角色（用户 a）可访问，越权时 Shiro 抛 AuthorizationException。
	 */
	@RequiresPermissions("user:list")
	@GetMapping("/users")
	public String users(Model model) {
		model.addAttribute("users", userService.listUsers());
		return "users";
	}

	/**
	 * 订单管理：仅 order_admin 角色（用户 b）可访问。
	 */
	@RequiresPermissions("order:list")
	@GetMapping("/orders")
	public String orders(Model model) {
		model.addAttribute("orders", userService.listOrders());
		return "orders";
	}

	/**
	 * 越权提示页。
	 */
	@GetMapping("/403")
	public String forbidden() {
		return "403";
	}

}

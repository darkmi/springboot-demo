package com.darkmi.springboot.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 给所有页面注入菜单可见性标记：有 user:list 权限才显示"用户管理"，
 * 有 order:list 才显示"订单管理"（与 SecurityConfig 的 URL 规则使用同一份权限）。
 */
@ControllerAdvice
public class MenuAdvice {

	@ModelAttribute("canManageUsers")
	public boolean canManageUsers() {
		return hasPermission("user:list");
	}

	@ModelAttribute("canManageOrders")
	public boolean canManageOrders() {
		return hasPermission("order:list");
	}

	private boolean hasPermission(String permission) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(permission));
	}

}

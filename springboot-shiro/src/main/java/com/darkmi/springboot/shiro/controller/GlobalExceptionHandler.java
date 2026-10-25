package com.darkmi.springboot.shiro.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全局处理：
 * 1. 给所有页面注入菜单可见性标记（按当前用户权限）；
 * 2. 捕获 @RequiresPermissions 抛出的 AuthorizationException，渲染 403 页。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 供模板菜单项 th:if 判断：有 user:list 权限才显示“用户管理”，有 order:list 才显示“订单管理”。
	 */
	@ModelAttribute
	public void appendMenuFlags(Model model) {
		Subject subject = SecurityUtils.getSubject();
		model.addAttribute("canManageUsers", subject.isPermitted("user:list"));
		model.addAttribute("canManageOrders", subject.isPermitted("order:list"));
	}

	@ExceptionHandler(AuthorizationException.class)
	public String handleAuthorizationException() {
		return "403";
	}

}

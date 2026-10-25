package com.darkmi.springboot.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 装配：表单登录、URL 级权限拦截、越权跳 403 页。
 * /users 需 user:list 权限（仅用户 a），/orders 需 order:list 权限（仅用户 b），
 * 其余请求登录即可。CSRF 保持默认开启，Thymeleaf th:action 会自动携带 token。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				// 登录页与静态资源放行
				.requestMatchers("/login", "/css/**", "/favicon.ico").permitAll()
				// 用户管理：仅 user_admin 角色（用户 a）对应的权限
				.requestMatchers("/users", "/users/**").hasAuthority("user:list")
				// 订单管理：仅 order_admin 角色（用户 b）对应的权限
				.requestMatchers("/orders", "/orders/**").hasAuthority("order:list")
				// 其余（含个人中心、/403）要求已登录
				.anyRequest().authenticated())
			.formLogin(form -> form
				// 自定义登录页；POST /login 由框架处理，失败重定向 /login?error
				.loginPage("/login")
				.defaultSuccessUrl("/", true)
				.permitAll())
			.logout(logout -> logout
				// 默认处理 POST /logout，成功后回登录页
				.logoutSuccessUrl("/login"))
			.exceptionHandling(ex -> ex
				// 已登录但无权限时跳转 403 页（Security 7 中由 accessDeniedUrl 改名为 accessDeniedPage）
				.accessDeniedPage("/403"));
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// BCrypt 自带随机盐，密码哈希由 DataInitializer 生成
		return new BCryptPasswordEncoder();
	}

}

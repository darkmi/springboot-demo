package com.darkmi.springboot.shiro.config;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.Filter;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.darkmi.springboot.shiro.realm.UserRealm;
import com.darkmi.springboot.shiro.service.UserService;

/**
 * Shiro 手动装配：Realm → SecurityManager → 过滤器链 → 注解授权 Advisor。
 * 不依赖 starter 自动配置，全部 Bean 显式可控。
 */
@Configuration
public class ShiroConfig {

	@Bean
	public UserRealm userRealm(UserService userService) {
		return new UserRealm(userService);
	}

	@Bean
	public DefaultWebSecurityManager securityManager(UserRealm userRealm) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(userRealm);
		// @RequiresPermissions 注解通过 SecurityUtils 获取 SecurityManager，需显式设置静态引用
		SecurityUtils.setSecurityManager(securityManager);
		return securityManager;
	}

	@Bean
	public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
		ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
		factoryBean.setSecurityManager(securityManager);
		factoryBean.setLoginUrl("/login");
		factoryBean.setUnauthorizedUrl("/403");
		Map<String, String> filterChain = new LinkedHashMap<>();
		// 登录接口放行（登录动作在 LoginController 中手动调用 Subject.login）
		filterChain.put("/login", "anon");
		// 退出登录走 Shiro 自带 logout 过滤器
		filterChain.put("/logout", "logout");
		// 静态资源放行
		filterChain.put("/css/**", "anon");
		filterChain.put("/favicon.ico", "anon");
		// 其余全部要求已登录，未登录重定向到 /login
		filterChain.put("/**", "authc");
		factoryBean.setFilterChainDefinitionMap(filterChain);
		return factoryBean;
	}

	/**
	 * 把 Shiro 过滤器注册进 Servlet 容器。Shiro 3.x 的 ShiroFilterFactoryBean 实现
	 * FactoryBean&lt;jakarta.servlet.Filter&gt;，显式包一层 FilterRegistrationBean，行为在 Boot 4 下更确定。
	 */
	@Bean
	public FilterRegistrationBean<Filter> shiroFilterRegistration(ShiroFilterFactoryBean factoryBean)
			throws Exception {
		FilterRegistrationBean<Filter> registration =
				new FilterRegistrationBean<>(factoryBean.getObject());
		registration.addUrlPatterns("/*");
		registration.setName("shiroFilter");
		// 先于 Spring MVC 的 DispatcherServlet 执行
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}

	/**
	 * 启用 @RequiresPermissions / @RequiresRoles 等注解授权（配合 Spring AOP 代理 Controller）。
	 */
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
			SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
		advisor.setSecurityManager(securityManager);
		return advisor;
	}

}

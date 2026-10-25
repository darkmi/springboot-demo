package com.darkmi.springboot.shiro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.darkmi.springboot.shiro.realm.UserRealm;

/**
 * 直接验证 Realm 的认证与授权逻辑（测试库落在 build/ 下，不污染模块根目录的演示库）。
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:sqlite:build/shiro-test.db",
		"spring.datasource.hikari.maximum-pool-size=1"
})
class SpringbootShiroApplicationTests {

	@Autowired
	private UserRealm userRealm;

	@Test
	@DisplayName("用户 a 用 123456 可认证，拥有 user:list 权限")
	void userAAuthenticatesAndHasUserPermission() {
		AuthenticationInfo info = userRealm.getAuthenticationInfo(new UsernamePasswordToken("a", "123456"));
		assertThat(info).isNotNull();

		SimplePrincipalCollection principals = new SimplePrincipalCollection("a", userRealm.getName());
		assertThat(userRealm.hasRole(principals, "user_admin")).isTrue();
		assertThat(userRealm.isPermitted(principals, "user:list")).isTrue();
		assertThat(userRealm.isPermitted(principals, "order:list")).isFalse();
	}

	@Test
	@DisplayName("用户 b 用 123456 可认证，拥有 order:list 权限")
	void userBAuthenticatesAndHasOrderPermission() {
		AuthenticationInfo info = userRealm.getAuthenticationInfo(new UsernamePasswordToken("b", "123456"));
		assertThat(info).isNotNull();

		SimplePrincipalCollection principals = new SimplePrincipalCollection("b", userRealm.getName());
		assertThat(userRealm.hasRole(principals, "order_admin")).isTrue();
		assertThat(userRealm.isPermitted(principals, "order:list")).isTrue();
		assertThat(userRealm.isPermitted(principals, "user:list")).isFalse();
	}

	@Test
	@DisplayName("密码错误时认证抛出 IncorrectCredentialsException")
	void wrongPasswordFails() {
		assertThat(catchThrowable(() -> userRealm.getAuthenticationInfo(new UsernamePasswordToken("a", "wrong"))))
				.isInstanceOf(IncorrectCredentialsException.class);
	}

}

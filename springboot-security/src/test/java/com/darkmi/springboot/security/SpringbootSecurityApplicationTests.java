package com.darkmi.springboot.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.darkmi.springboot.security.security.DemoUserDetailsService;

/**
 * 验证 UserDetailsService 的用户加载、权限赋予与 BCrypt 密码匹配
 * （测试库落在 build/ 下，不污染模块根目录的演示库）。
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:sqlite:build/security-test.db",
		"spring.datasource.hikari.maximum-pool-size=1"
})
class SpringbootSecurityApplicationTests {

	@Autowired
	private DemoUserDetailsService userDetailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("用户 a 携带 user:list 权限，密码 123456 可匹配")
	void userAHasUserPermission() {
		UserDetails user = userDetailsService.loadUserByUsername("a");
		assertThat(user.getUsername()).isEqualTo("a");
		assertThat(authoritiesOf(user))
				.contains("user:list", "ROLE_user_admin")
				.doesNotContain("order:list");
		assertThat(passwordEncoder.matches("123456", user.getPassword())).isTrue();
	}

	@Test
	@DisplayName("用户 b 携带 order:list 权限，密码 123456 可匹配")
	void userBHasOrderPermission() {
		UserDetails user = userDetailsService.loadUserByUsername("b");
		assertThat(user.getUsername()).isEqualTo("b");
		assertThat(authoritiesOf(user))
				.contains("order:list", "ROLE_order_admin")
				.doesNotContain("user:list");
		assertThat(passwordEncoder.matches("123456", user.getPassword())).isTrue();
	}

	@Test
	@DisplayName("不存在的用户名抛出 UsernameNotFoundException")
	void unknownUserThrows() {
		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nobody"))
				.isInstanceOf(UsernameNotFoundException.class);
	}

	private Set<String> authoritiesOf(UserDetails user) {
		return user.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());
	}

}

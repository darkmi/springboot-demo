package com.darkmi.springboot.security.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.darkmi.springboot.security.service.UserService;

/**
 * 从 sys_user / sys_role / sys_role_permission 加载用户。
 * 权限（user:list 等）与角色（ROLE_ 前缀）都作为 GrantedAuthority，
 * URL 规则用权限判断，模板菜单标记也从同一份 authorities 取。
 */
@Service
public class DemoUserDetailsService implements UserDetailsService {

	private final UserService userService;

	public DemoUserDetailsService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		com.darkmi.springboot.security.entity.User user = userService.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("用户不存在: " + username);
		}
		Set<String> roles = userService.findRoles(username);
		Set<String> permissions = userService.findPermissions(username);

		List<GrantedAuthority> authorities = new ArrayList<>(permissions.size() + roles.size());
		permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
		// 角色统一加 ROLE_ 前缀，遵循 Spring Security 的 hasRole 约定
		roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));

		// BCrypt 哈希直接作为 credentials，enabled 等状态位默认 true
		return User.withUsername(user.getUsername())
				.password(user.getPassword())
				.authorities(authorities)
				.build();
	}

}

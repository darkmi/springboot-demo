package com.darkmi.springboot.security.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.darkmi.springboot.security.entity.User;

/**
 * 用户/角色/权限/订单数据访问，基于 JdbcTemplate 查询 SQLite。
 */
@Component
public class UserService {

	private final JdbcTemplate jdbcTemplate;

	public UserService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 按用户名查用户，不存在返回 null。
	 */
	public User findByUsername(String username) {
		List<User> users = jdbcTemplate.query(
				"SELECT id, username, password, salt, nickname FROM sys_user WHERE username = ?",
				(rs, rowNum) -> {
					User user = new User();
					user.setId(rs.getLong("id"));
					user.setUsername(rs.getString("username"));
					user.setPassword(rs.getString("password"));
					user.setSalt(rs.getString("salt"));
					user.setNickname(rs.getString("nickname"));
					return user;
				},
				username);
		return users.isEmpty() ? null : users.get(0);
	}

	/**
	 * 查用户拥有的角色名集合。
	 */
	public Set<String> findRoles(String username) {
		return new LinkedHashSet<>(jdbcTemplate.queryForList(
				"SELECT r.role_name FROM sys_role r "
						+ "JOIN sys_user_role ur ON r.id = ur.role_id "
						+ "JOIN sys_user u ON u.id = ur.user_id "
						+ "WHERE u.username = ?",
				String.class, username));
	}

	/**
	 * 查用户经角色聚合后的权限集合。
	 */
	public Set<String> findPermissions(String username) {
		return new LinkedHashSet<>(jdbcTemplate.queryForList(
				"SELECT DISTINCT p.permission FROM sys_role_permission p "
						+ "JOIN sys_user_role ur ON p.role_id = ur.role_id "
						+ "JOIN sys_user u ON u.id = ur.user_id "
						+ "WHERE u.username = ?",
				String.class, username));
	}

	/**
	 * 用户管理页：用户列表（含角色，逗号分隔）。
	 */
	public List<Map<String, Object>> listUsers() {
		return jdbcTemplate.queryForList(
				"SELECT u.username, u.nickname, "
						+ "COALESCE(GROUP_CONCAT(r.role_name, ', '), '') AS roles "
						+ "FROM sys_user u "
						+ "LEFT JOIN sys_user_role ur ON u.id = ur.user_id "
						+ "LEFT JOIN sys_role r ON r.id = ur.role_id "
						+ "GROUP BY u.id, u.username, u.nickname "
						+ "ORDER BY u.id");
	}

	/**
	 * 订单管理页：订单列表。
	 */
	public List<Map<String, Object>> listOrders() {
		return jdbcTemplate.queryForList(
				"SELECT order_no, amount, status FROM orders ORDER BY id");
	}

}

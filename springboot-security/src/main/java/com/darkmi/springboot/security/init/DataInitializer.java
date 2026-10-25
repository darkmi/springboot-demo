package com.darkmi.springboot.security.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 幂等初始化演示数据：用户 a（用户管理）、用户 b（订单管理）及角色权限、示例订单。
 * 密码统一为 123456，BCrypt 哈希后入库（salt 列固定存空串，BCrypt 自带盐）。
 */
@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private static final String DEFAULT_PASSWORD = "123456";

	private final JdbcTemplate jdbcTemplate;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
		this.jdbcTemplate = jdbcTemplate;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
		if (userCount != null && userCount > 0) {
			return;
		}
		log.info("检测到空库，初始化演示数据……");

		// 角色
		jdbcTemplate.update("INSERT INTO sys_role(role_name) VALUES ('user_admin'), ('order_admin')");
		// 角色权限：user_admin 管用户，order_admin 管订单
		jdbcTemplate.update(
				"INSERT INTO sys_role_permission(role_id, permission) VALUES "
						+ "((SELECT id FROM sys_role WHERE role_name = 'user_admin'), 'user:list'), "
						+ "((SELECT id FROM sys_role WHERE role_name = 'order_admin'), 'order:list')");

		// 用户 a / b 及其角色
		createUser("a", "用户A", "user_admin");
		createUser("b", "用户B", "order_admin");

		// 示例订单（仅用户 b 可见）
		jdbcTemplate.update(
				"INSERT INTO orders(order_no, amount, status) VALUES "
						+ "('ORD-20261025-001', 128.50, 'PAID'), "
						+ "('ORD-20261025-002', 399.00, 'SHIPPED'), "
						+ "('ORD-20261025-003', 66.00, 'COMPLETED')");

		log.info("演示数据初始化完成：用户 a/123456（用户管理）、用户 b/123456（订单管理）");
	}

	/**
	 * 写入用户并绑定角色：BCrypt 哈希密码。
	 */
	private void createUser(String username, String nickname, String roleName) {
		String hash = passwordEncoder.encode(DEFAULT_PASSWORD);
		jdbcTemplate.update(
				"INSERT INTO sys_user(username, password, salt, nickname) VALUES (?, ?, '', ?)",
				username, hash, nickname);
		jdbcTemplate.update(
				"INSERT INTO sys_user_role(user_id, role_id) "
						+ "SELECT u.id, r.id FROM sys_user u, sys_role r "
						+ "WHERE u.username = ? AND r.role_name = ?",
				username, roleName);
	}

}

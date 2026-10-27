package com.darkmi.springboot.shiro.init;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 幂等初始化演示数据：admin（全部功能）、useradmin（用户管理）、orderadmin（订单管理）
 * 及角色权限、示例订单。密码统一为 123456，SHA-256 加盐 16 轮哈希后入库。
 */
@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private static final String HASH_ALGORITHM = "SHA-256";
	private static final int HASH_ITERATIONS = 16;
	private static final String DEFAULT_PASSWORD = "123456";

	private final JdbcTemplate jdbcTemplate;

	public DataInitializer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(String... args) {
		Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
		if (userCount != null && userCount > 0) {
			return;
		}
		log.info("检测到空库，初始化演示数据……");

		// 角色
		jdbcTemplate.update(
				"INSERT INTO sys_role(role_name) VALUES ('super_admin'), ('user_admin'), ('order_admin')");
		// 角色权限：super_admin 拥有全部权限，user_admin 管用户，order_admin 管订单
		jdbcTemplate.update(
				"INSERT INTO sys_role_permission(role_id, permission) VALUES "
						+ "((SELECT id FROM sys_role WHERE role_name = 'super_admin'), 'user:list'), "
						+ "((SELECT id FROM sys_role WHERE role_name = 'super_admin'), 'order:list'), "
						+ "((SELECT id FROM sys_role WHERE role_name = 'user_admin'), 'user:list'), "
						+ "((SELECT id FROM sys_role WHERE role_name = 'order_admin'), 'order:list')");

		// 三个演示用户及其角色
		createUser("admin", "超级管理员", "super_admin");
		createUser("useradmin", "用户管理员", "user_admin");
		createUser("orderadmin", "订单管理员", "order_admin");

		// 示例订单（仅 admin / orderadmin 可见）
		jdbcTemplate.update(
				"INSERT INTO orders(order_no, amount, status) VALUES "
						+ "('ORD-20261025-001', 128.50, 'PAID'), "
						+ "('ORD-20261025-002', 399.00, 'SHIPPED'), "
						+ "('ORD-20261025-003', 66.00, 'COMPLETED')");

		log.info("演示数据初始化完成：admin/123456（全部功能）、useradmin/123456（用户管理）、orderadmin/123456（订单管理）");
	}

	/**
	 * 写入用户并绑定角色：随机盐 + SHA-256 哈希密码。
	 */
	private void createUser(String username, String nickname, String roleName) {
		String salt = new SecureRandomNumberGenerator().nextBytes(16).toHex();
		String hash = new SimpleHash(HASH_ALGORITHM, DEFAULT_PASSWORD, salt, HASH_ITERATIONS).toHex();
		jdbcTemplate.update(
				"INSERT INTO sys_user(username, password, salt, nickname) VALUES (?, ?, ?, ?)",
				username, hash, salt, nickname);
		jdbcTemplate.update(
				"INSERT INTO sys_user_role(user_id, role_id) "
						+ "SELECT u.id, r.id FROM sys_user u, sys_role r "
						+ "WHERE u.username = ? AND r.role_name = ?",
				username, roleName);
	}

}

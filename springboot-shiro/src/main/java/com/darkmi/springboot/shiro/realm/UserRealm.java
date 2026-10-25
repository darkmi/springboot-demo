package com.darkmi.springboot.shiro.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.lang.util.ByteSource;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.darkmi.springboot.shiro.entity.User;
import com.darkmi.springboot.shiro.service.UserService;

/**
 * 自定义 Realm：认证查 sys_user（SHA-256 加盐），授权查用户的角色与权限。
 */
public class UserRealm extends AuthorizingRealm {

	/** 与 DataInitializer 入库哈希保持一致：SHA-256 加盐 16 轮 */
	private static final String HASH_ALGORITHM = "SHA-256";
	private static final int HASH_ITERATIONS = 16;

	private final UserService userService;

	public UserRealm(UserService userService) {
		this.userService = userService;
		// 库里存的是加盐哈希，须用 HashedCredentialsMatcher 做同参数哈希后再比对
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(HASH_ALGORITHM);
		matcher.setHashIterations(HASH_ITERATIONS);
		setCredentialsMatcher(matcher);
	}

	/**
	 * 授权：把角色和权限塞进 AuthorizationInfo，供 @RequiresPermissions / 页面菜单判断使用。
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String username = (String) principals.getPrimaryPrincipal();
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.setRoles(userService.findRoles(username));
		info.setStringPermissions(userService.findPermissions(username));
		return info;
	}

	/**
	 * 认证：按用户名查库，返回加盐哈希交给凭据匹配器比对；用户不存在返回 null 触发 UnknownAccountException。
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
		User user = userService.findByUsername((String) token.getPrincipal());
		if (user == null) {
			return null;
		}
		return new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(),
				ByteSource.Util.bytes(user.getSalt()), getName());
	}

}

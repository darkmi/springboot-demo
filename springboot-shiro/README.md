# springboot-shiro

基于 Apache Shiro + SQLite 的登录授权示例：表单登录、菜单与页面按角色/权限控制。

> 注意：本模块基于 Spring Boot 4（Tomcat 11 / Jakarta EE），必须使用 **Shiro 3.x**——
> 2.x 全系仍依赖 `javax.servlet`，与 jakarta 容器运行时不兼容。代码为手动装配
> （Realm / SecurityManager / 过滤器链 / 注解 Advisor），不依赖 shiro starter 自动配置。

## 功能

- 登录页（`/login`），未登录访问任何页面自动重定向到登录页
- SQLite 文件数据库（`shiro-demo.db`），启动自动建表并幂等初始化演示数据
- 三个菜单：**个人中心** / **用户管理** / **订单管理**
  - 个人中心：登录即可访问
  - 用户管理：需 `user:list` 权限（仅用户 a），越权访问返回 403 页
  - 订单管理：需 `order:list` 权限（仅用户 b），越权访问返回 403 页
- 菜单项按当前用户权限显隐；Shiro 过滤器链（authc/logout/anon）拦截未登录请求；
  `@RequiresPermissions` 注解做方法级授权

## 演示账号

| 用户名 | 密码   | 角色        | 可访问             |
|--------|--------|-------------|--------------------|
| a      | 123456 | user_admin  | 个人中心、用户管理 |
| b      | 123456 | order_admin | 个人中心、订单管理 |

## 运行

```bash
./gradlew :springboot-shiro:bootRun
```

访问 <http://localhost:9095>。

## 数据模型

- `sys_user` / `sys_role` / `sys_user_role` / `sys_role_permission`：用户-角色-权限
- 密码存储为 SHA-256 加盐 16 轮哈希（`DataInitializer` 生成随机盐）
- `orders`：订单演示数据，仅订单管理页展示

# springboot-security

基于 Spring Security + SQLite 的登录授权示例：表单登录、菜单与页面按角色/权限控制。
与 `springboot-shiro` 模块功能对齐，方便对比两套安全框架的实现方式。

## 功能

- 登录页（`/login`），未登录访问任何页面自动重定向到登录页
- SQLite 文件数据库（`security-demo.db`），启动自动建表并幂等初始化演示数据
- 三个菜单：**个人中心** / **用户管理** / **订单管理**
  - 个人中心：登录即可访问
  - 用户管理：需 `user:list` 权限（仅用户 a），越权访问返回 403 页
  - 订单管理：需 `order:list` 权限（仅用户 b），越权访问返回 403 页
- 菜单项按当前用户权限显隐（`MenuAdvice` 注入模板标记）
- URL 级权限规则（`SecurityFilterChain`）拦截越权请求；CSRF 默认开启
- 密码使用 BCrypt 哈希存储

## 演示账号

| 用户名 | 密码   | 角色        | 可访问             |
|--------|--------|-------------|--------------------|
| a      | 123456 | user_admin  | 个人中心、用户管理 |
| b      | 123456 | order_admin | 个人中心、订单管理 |

## 运行

```bash
./gradlew :springboot-security:bootRun
```

访问 <http://localhost:9096>。

## 数据模型

- `sys_user` / `sys_role` / `sys_user_role` / `sys_role_permission`：用户-角色-权限
- 密码存储为 BCrypt 哈希（自带随机盐；`salt` 列仅为与 shiro 模块表结构一致而保留，固定空串）
- `orders`：订单演示数据，仅订单管理页展示

## 与 shiro 模块的对应关系

| 关注点         | springboot-shiro                      | springboot-security                |
|----------------|---------------------------------------|------------------------------------|
| 认证           | `Subject.login` + 自定义 Realm        | `UserDetailsService` + BCrypt      |
| 页面拦截       | 过滤器链 authc + `@RequiresPermissions` | `SecurityFilterChain` URL 规则   |
| 越权处理       | `AuthorizationException` → 403 页     | `accessDeniedPage("/403")`         |
| 菜单显隐       | `Subject.isPermitted`                 | `Authentication` authorities       |

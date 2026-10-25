-- 用户表（password 为 Shiro SHA-256 加盐哈希，演示数据由 DataInitializer 幂等初始化）
CREATE TABLE IF NOT EXISTS sys_user (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    salt     TEXT NOT NULL,
    nickname TEXT NOT NULL
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    role_name TEXT NOT NULL UNIQUE
);

-- 用户-角色关联
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 角色-权限关联
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id    INTEGER NOT NULL,
    permission TEXT    NOT NULL,
    PRIMARY KEY (role_id, permission)
);

-- 订单表（演示数据）
CREATE TABLE IF NOT EXISTS orders (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no TEXT NOT NULL,
    amount   REAL NOT NULL,
    status   TEXT NOT NULL
);

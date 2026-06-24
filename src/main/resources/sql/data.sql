-- 初始管理员用户 (密码: admin123 的 BCrypt 加密)
INSERT INTO sys_user (username, password, nickname, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '超级管理员', 1);

-- 初始角色
INSERT INTO sys_role (role_name, role_code, description, status) VALUES
('超级管理员', 'ADMIN', '拥有所有权限', 1),
('普通用户', 'USER', '普通用户权限', 1);

-- 初始菜单（目录 + 菜单 + 按钮）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, icon, sort_order) VALUES
-- 系统管理目录
(1, 0, '系统管理', 1, '/system', NULL, 'system', 1),
-- 用户管理
(2, 1, '用户管理', 2, '/system/user', 'sys:user:list', 'user', 10),
(3, 2, '新增用户', 3, NULL, 'sys:user:add', NULL, 11),
(4, 2, '修改用户', 3, NULL, 'sys:user:edit', NULL, 12),
(5, 2, '删除用户', 3, NULL, 'sys:user:delete', NULL, 13),
(6, 2, '分配角色', 3, NULL, 'sys:user:role', NULL, 14),
(7, 2, '重置密码', 3, NULL, 'sys:user:resetPwd', NULL, 15),
-- 角色管理
(8, 1, '角色管理', 2, '/system/role', 'sys:role:list', 'role', 20),
(9, 8, '新增角色', 3, NULL, 'sys:role:add', NULL, 21),
(10, 8, '修改角色', 3, NULL, 'sys:role:edit', NULL, 22),
(11, 8, '删除角色', 3, NULL, 'sys:role:delete', NULL, 23),
(12, 8, '分配菜单', 3, NULL, 'sys:role:menu', NULL, 24),
-- 菜单管理
(13, 1, '菜单管理', 2, '/system/menu', 'sys:menu:list', 'menu', 30),
(14, 13, '新增菜单', 3, NULL, 'sys:menu:add', NULL, 31),
(15, 13, '修改菜单', 3, NULL, 'sys:menu:edit', NULL, 32),
(16, 13, '删除菜单', 3, NULL, 'sys:menu:delete', NULL, 33),
-- 日志管理目录
(17, 0, '日志管理', 1, '/log', NULL, 'log', 2),
(18, 17, '操作日志', 2, '/log/operation', 'sys:log:list', 'documentation', 40),
(19, 17, '登录日志', 2, '/log/login', 'sys:log:list', 'logininfor', 50);

-- 初始角色-菜单关联（ADMIN 拥有全部菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 初始用户-角色关联（admin 为 ADMIN）
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

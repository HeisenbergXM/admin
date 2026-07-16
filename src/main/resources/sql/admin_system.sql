/*
 Navicat Premium Dump SQL

 Source Server         : AI练手
 Source Server Type    : MySQL
 Source Server Version : 80410 (8.4.10-0ubuntu0.26.04.1)
 Source Host           : 120.26.184.222:3306
 Source Schema         : admin_system

 Target Server Type    : MySQL
 Target Server Version : 80410 (8.4.10-0ubuntu0.26.04.1)
 File Encoding         : 65001

 Date: 06/07/2026 22:26:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录用户名',
  `login_type` tinyint NOT NULL COMMENT '类型：1登录 2登出',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录IP',
  `location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录地点',
  `status` tinyint NOT NULL COMMENT '状态：0失败 1成功',
  `message` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提示消息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '登录日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
INSERT INTO `sys_login_log` VALUES (1, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 0, '用户名或密码错误', '2026-06-25 20:21:58');
INSERT INTO `sys_login_log` VALUES (2, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 0, '用户名或密码错误', '2026-06-25 20:23:01');
INSERT INTO `sys_login_log` VALUES (3, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 0, '用户名或密码错误', '2026-06-25 20:23:25');
INSERT INTO `sys_login_log` VALUES (4, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 0, '用户名或密码错误', '2026-06-25 20:28:23');
INSERT INTO `sys_login_log` VALUES (5, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 0, '用户名或密码错误', '2026-06-25 20:28:25');
INSERT INTO `sys_login_log` VALUES (6, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-06-25 20:37:19');
INSERT INTO `sys_login_log` VALUES (7, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-06-25 20:48:27');
INSERT INTO `sys_login_log` VALUES (8, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-06-25 21:15:39');
INSERT INTO `sys_login_log` VALUES (9, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-06-25 21:25:31');
INSERT INTO `sys_login_log` VALUES (10, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-01 22:20:29');
INSERT INTO `sys_login_log` VALUES (11, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-01 22:20:41');
INSERT INTO `sys_login_log` VALUES (12, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-01 22:20:41');
INSERT INTO `sys_login_log` VALUES (13, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-01 22:20:51');
INSERT INTO `sys_login_log` VALUES (14, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-01 22:21:01');
INSERT INTO `sys_login_log` VALUES (15, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-01 22:22:02');
INSERT INTO `sys_login_log` VALUES (16, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-01 22:22:02');
INSERT INTO `sys_login_log` VALUES (17, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-01 22:22:30');
INSERT INTO `sys_login_log` VALUES (18, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-02 22:12:06');
INSERT INTO `sys_login_log` VALUES (19, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-02 22:12:06');
INSERT INTO `sys_login_log` VALUES (20, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-02 22:12:57');
INSERT INTO `sys_login_log` VALUES (21, 'admin', 1, '0:0:0:0:0:0:0:1', NULL, 1, '登录成功', '2026-07-02 22:13:11');
INSERT INTO `sys_login_log` VALUES (22, 'admin', 1, '112.31.213.65', NULL, 1, '登录成功', '2026-07-06 19:28:05');
INSERT INTO `sys_login_log` VALUES (23, 'admin', 1, '58.144.138.16', NULL, 1, '登录成功', '2026-07-06 20:11:03');
INSERT INTO `sys_login_log` VALUES (24, 'admin', 1, '58.144.138.16', NULL, 1, '登录成功', '2026-07-06 20:19:37');
INSERT INTO `sys_login_log` VALUES (25, 'admin', 1, '112.31.213.65', NULL, 1, '登录成功', '2026-07-06 20:20:26');
INSERT INTO `sys_login_log` VALUES (26, 'test', 1, '58.144.138.16', NULL, 1, '登录成功', '2026-07-06 20:38:02');
INSERT INTO `sys_login_log` VALUES (27, 'test', 1, '58.144.138.16', NULL, 1, '登录成功', '2026-07-06 20:38:57');
INSERT INTO `sys_login_log` VALUES (28, 'test', 1, '58.144.138.16', NULL, 1, '登录成功', '2026-07-06 20:40:20');
INSERT INTO `sys_login_log` VALUES (29, 'admin', 1, '58.144.138.16', NULL, 1, '登录成功', '2026-07-06 20:43:38');
INSERT INTO `sys_login_log` VALUES (30, 'admin', 1, '112.31.213.65', NULL, 1, '登录成功', '2026-07-06 20:54:10');
INSERT INTO `sys_login_log` VALUES (31, 'admin', 1, '58.144.138.16', NULL, 1, '登录成功', '2026-07-06 21:42:38');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单ID（0为顶级）',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '类型：1目录 2菜单 3按钮',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由路径',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1092 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单/权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', 1, '/system', NULL, 'system', 1, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (2, 1, '用户管理', 2, '/system/user', 'sys:user:list', 'user', 10, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (3, 2, '新增用户', 3, NULL, 'sys:user:add', NULL, 11, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (4, 2, '修改用户', 3, NULL, 'sys:user:edit', NULL, 12, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (5, 2, '删除用户', 3, NULL, 'sys:user:delete', NULL, 13, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (6, 2, '分配角色', 3, NULL, 'sys:user:role', NULL, 14, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (7, 2, '重置密码', 3, NULL, 'sys:user:resetPwd', NULL, 15, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (8, 1, '角色管理', 2, '/system/role', 'sys:role:list', 'role', 20, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (9, 8, '新增角色', 3, NULL, 'sys:role:add', NULL, 21, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (10, 8, '修改角色', 3, NULL, 'sys:role:edit', NULL, 22, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (11, 8, '删除角色', 3, NULL, 'sys:role:delete', NULL, 23, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (12, 8, '分配菜单', 3, NULL, 'sys:role:menu', NULL, 24, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (13, 1, '菜单管理', 2, '/system/menu', 'sys:menu:list', 'menu', 30, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (14, 13, '新增菜单', 3, NULL, 'sys:menu:add', NULL, 31, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (15, 13, '修改菜单', 3, NULL, 'sys:menu:edit', NULL, 32, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (16, 13, '删除菜单', 3, NULL, 'sys:menu:delete', NULL, 33, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (17, 0, '日志管理', 1, '/log', NULL, 'log', 2, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (18, 17, '操作日志', 2, '/log/operation', 'sys:log:list', 'documentation', 40, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (19, 17, '登录日志', 2, '/log/login', 'sys:log:list', 'logininfor', 50, 1, '2026-07-02 22:12:30', '2026-07-02 22:12:30', 0);
INSERT INTO `sys_menu` VALUES (100, 1, '字典管理', 2, '/system/dict', 'sys:dict:list', 'dict', 60, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (110, 0, '主数据管理', 1, '/master-data', NULL, 'master', 100, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (111, 110, '车型数据', 2, '/master-data/model', 'sys:master:list', 'model', 101, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (112, 110, '外饰颜色', 2, '/master-data/exterior', 'sys:master:list', 'color', 102, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (113, 110, '内饰颜色', 2, '/master-data/interior', 'sys:master:list', 'color', 103, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (114, 110, '经销商维护', 2, '/master-data/dealer', 'sys:master:list', 'dealer', 104, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (120, 0, '生产管理', 1, '/production', NULL, 'production', 110, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (121, 120, '车辆录入', 2, '/production/vehicle', 'sys:vehicle:list', 'vehicle', 111, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (130, 0, '运输管理', 1, '/transport', NULL, 'transport', 120, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (131, 130, '车厂到仓库', 2, '/transport/inbound', 'vlm:transport:list', 'inbound', 121, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (132, 130, '仓库到经销商', 2, '/transport/outbound', 'vlm:dispatch:list', 'outbound', 122, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (140, 0, '销售管理', 1, '/sales', NULL, 'sales', 130, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (141, 140, '车辆销售', 2, '/sales/allocation', 'vlm:allocation:list', 'allocation', 131, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (142, 140, '车辆上牌', 2, '/sales/registration', 'vlm:registration:list', 'registration', 132, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (150, 0, '财务管理', 1, '/finance', NULL, 'finance', 140, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (151, 150, '发票确认', 2, '/finance/invoice', 'vlm:invoice:list', 'invoice', 141, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (152, 150, '收款确认', 2, '/finance/payment', 'vlm:payment:list', 'payment', 142, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (160, 0, '车辆全景视图', 2, '/panorama', 'vlm:panorama:view', 'panorama', 150, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1000, 100, '字典查询', 3, NULL, 'sys:dict:list', NULL, 1000, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1001, 100, '字典新增', 3, NULL, 'sys:dict:add', NULL, 1001, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1002, 100, '字典编辑', 3, NULL, 'sys:dict:edit', NULL, 1002, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1003, 100, '字典删除', 3, NULL, 'sys:dict:delete', NULL, 1003, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1010, 110, '主数据查询', 3, NULL, 'sys:master:list', NULL, 1010, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1011, 110, '主数据新增', 3, NULL, 'sys:master:add', NULL, 1011, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1012, 110, '主数据编辑', 3, NULL, 'sys:master:edit', NULL, 1012, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1013, 110, '主数据删除', 3, NULL, 'sys:master:delete', NULL, 1013, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1020, 121, '车辆查询', 3, NULL, 'sys:vehicle:list', NULL, 1020, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1021, 121, '车辆新增', 3, NULL, 'sys:vehicle:add', NULL, 1021, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1022, 121, '车辆编辑', 3, NULL, 'sys:vehicle:edit', NULL, 1022, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1023, 121, '车辆确认', 3, NULL, 'sys:vehicle:confirm', NULL, 1023, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1030, 131, '运输单查询', 3, NULL, 'vlm:transport:list', NULL, 1030, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1031, 131, '运输单新增', 3, NULL, 'vlm:transport:add', NULL, 1031, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1032, 131, '运输单编辑', 3, NULL, 'vlm:transport:edit', NULL, 1032, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1033, 131, '运输单确认', 3, NULL, 'vlm:transport:confirm', NULL, 1033, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1040, 132, '发车清单查询', 3, NULL, 'vlm:dispatch:list', NULL, 1040, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1041, 132, '发车清单新增', 3, NULL, 'vlm:dispatch:add', NULL, 1041, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1042, 132, '发车清单编辑', 3, NULL, 'vlm:dispatch:edit', NULL, 1042, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1043, 132, '发车清单确认', 3, NULL, 'vlm:dispatch:confirm', NULL, 1043, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1050, 141, '销售分配查询', 3, NULL, 'vlm:allocation:list', NULL, 1050, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1051, 141, '销售分配新增', 3, NULL, 'vlm:allocation:add', NULL, 1051, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1052, 141, '销售分配编辑', 3, NULL, 'vlm:allocation:edit', NULL, 1052, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1053, 141, '销售分配确认', 3, NULL, 'vlm:allocation:confirm', NULL, 1053, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1060, 142, '上牌查询', 3, NULL, 'vlm:registration:list', NULL, 1060, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1061, 142, '上牌新增', 3, NULL, 'vlm:registration:add', NULL, 1061, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1062, 142, '上牌编辑', 3, NULL, 'vlm:registration:edit', NULL, 1062, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1063, 142, '上牌确认', 3, NULL, 'vlm:registration:confirm', NULL, 1063, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1070, 151, '发票查询', 3, NULL, 'vlm:invoice:list', NULL, 1070, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1071, 151, '发票新增', 3, NULL, 'vlm:invoice:add', NULL, 1071, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1072, 151, '发票编辑', 3, NULL, 'vlm:invoice:edit', NULL, 1072, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1073, 151, '形式发票转正', 3, NULL, 'vlm:invoice:convert', NULL, 1073, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1074, 151, '发票确认', 3, NULL, 'vlm:invoice:confirm', NULL, 1074, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1080, 152, '收款查询', 3, NULL, 'vlm:payment:list', NULL, 1080, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1081, 152, '收款新增', 3, NULL, 'vlm:payment:add', NULL, 1081, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1082, 152, '收款编辑', 3, NULL, 'vlm:payment:edit', NULL, 1082, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1083, 152, '收款确认', 3, NULL, 'vlm:payment:confirm', NULL, 1083, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1090, 160, '全景查看', 3, NULL, 'vlm:panorama:view', NULL, 1090, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);
INSERT INTO `sys_menu` VALUES (1091, 160, '全景导出', 3, NULL, 'vlm:panorama:export', NULL, 1091, 1, '2026-07-01 22:53:08', '2026-07-01 22:53:08', 0);

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作用户名',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作描述',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '返回结果',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------
INSERT INTO `sys_operation_log` VALUES (1, NULL, 'admin', '查询操作日志', 'LogController.operationList', '[1,10]', '{\"code\":200,\"data\":{\"list\":[],\"total\":0,\"pageNum\":1,\"pageSize\":10},\"message\":\"操作成功\"}', '112.31.213.65', '2026-07-02 22:18:50');
INSERT INTO `sys_operation_log` VALUES (2, NULL, 'admin', '创建车辆生产草稿', 'VehProductionController.createProduction', '[{\"vin\":\"LSGAR5AL2MH123456\",\"modelId\":2,\"exteriorColorId\":4,\"interiorColorId\":4,\"engineNumber\":\"LSY-8B12345\",\"yearMake\":\"2025\",\"material\":\"后视镜总成-26493142\",\"shipment\":\"SH-2026-0713\",\"batch\":\"BATCH-B-002\",\"offlineEpmbDate\":null,\"epmbOkDate\":null,\"remark1\":\"\"}]', NULL, '58.144.138.16', '2026-07-06 20:23:47');
INSERT INTO `sys_operation_log` VALUES (3, NULL, 'admin', '确认生产录入', 'VehProductionController.confirmProduction', '[13]', NULL, '58.144.138.16', '2026-07-06 20:24:13');
INSERT INTO `sys_operation_log` VALUES (4, NULL, 'admin', '创建运输单', 'TransportOrderController.createOrder', NULL, NULL, '58.144.138.16', '2026-07-06 20:25:04');
INSERT INTO `sys_operation_log` VALUES (5, NULL, 'admin', '运输单添加 VIN', 'TransportOrderController.addItem', NULL, NULL, '58.144.138.16', '2026-07-06 20:25:15');
INSERT INTO `sys_operation_log` VALUES (6, NULL, 'admin', '编辑运输单', 'TransportOrderController.updateOrder', NULL, NULL, '58.144.138.16', '2026-07-06 20:25:18');
INSERT INTO `sys_operation_log` VALUES (7, NULL, 'admin', '编辑运输单', 'TransportOrderController.updateOrder', NULL, NULL, '58.144.138.16', '2026-07-06 20:25:43');
INSERT INTO `sys_operation_log` VALUES (8, NULL, 'admin', '确认运输单', 'TransportOrderController.confirmOrder', '[4]', NULL, '58.144.138.16', '2026-07-06 20:26:30');
INSERT INTO `sys_operation_log` VALUES (9, NULL, 'admin', '新增用户', 'UserController.create', '[{\"username\":\"test\",\"nickname\":\"测试用户\",\"email\":null,\"phone\":null,\"status\":1}]', NULL, '58.144.138.16', '2026-07-06 20:37:02');
INSERT INTO `sys_operation_log` VALUES (10, NULL, 'admin', '重置密码', 'UserController.resetPassword', '[2]', NULL, '58.144.138.16', '2026-07-06 20:37:18');
INSERT INTO `sys_operation_log` VALUES (11, NULL, 'admin', '分配用户角色', 'UserController.assignRoles', '[2,{\"userId\":2,\"roleIds\":[101]}]', NULL, '58.144.138.16', '2026-07-06 20:37:38');
INSERT INTO `sys_operation_log` VALUES (12, NULL, 'admin', '查询操作日志', 'LogController.operationList', '[1,10]', NULL, '58.144.138.16', '2026-07-06 21:48:25');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 106 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'ADMIN', '拥有所有权限', 1, '2026-06-23 23:01:14', '2026-06-23 23:01:14', 0);
INSERT INTO `sys_role` VALUES (2, '普通用户', 'USER', '普通用户权限', 1, '2026-06-23 23:01:14', '2026-06-23 23:01:14', 0);
INSERT INTO `sys_role` VALUES (100, '生产专员', 'PROD_SPECIALIST', '车辆生产数据录入', 1, '2026-06-25 21:41:12', '2026-06-25 21:41:12', 0);
INSERT INTO `sys_role` VALUES (101, '物流专员', 'LOGISTICS_SPECIALIST', '车厂到仓库、仓库到经销商配送', 1, '2026-06-25 21:41:12', '2026-06-25 21:41:12', 0);
INSERT INTO `sys_role` VALUES (102, '销售专员', 'SALES_SPECIALIST', '车辆销售分配、车辆上牌', 1, '2026-06-25 21:41:12', '2026-06-25 21:41:12', 0);
INSERT INTO `sys_role` VALUES (103, '财务专员', 'FINANCE_SPECIALIST', '发票确认、收款确认、形式发票转正', 1, '2026-06-25 21:41:12', '2026-06-25 21:41:12', 0);
INSERT INTO `sys_role` VALUES (104, '主数据管理员', 'MASTER_DATA_ADMIN', '车型/颜色/经销商主数据维护', 1, '2026-06-25 21:41:12', '2026-06-25 21:41:12', 0);
INSERT INTO `sys_role` VALUES (105, '业务主管', 'BUSINESS_MANAGER', '全局查看、车辆全景视图', 1, '2026-06-25 21:41:12', '2026-06-25 21:41:12', 0);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 266 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色-菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2, 1, 2);
INSERT INTO `sys_role_menu` VALUES (3, 1, 3);
INSERT INTO `sys_role_menu` VALUES (4, 1, 4);
INSERT INTO `sys_role_menu` VALUES (5, 1, 5);
INSERT INTO `sys_role_menu` VALUES (6, 1, 6);
INSERT INTO `sys_role_menu` VALUES (7, 1, 7);
INSERT INTO `sys_role_menu` VALUES (8, 1, 8);
INSERT INTO `sys_role_menu` VALUES (9, 1, 9);
INSERT INTO `sys_role_menu` VALUES (10, 1, 10);
INSERT INTO `sys_role_menu` VALUES (11, 1, 11);
INSERT INTO `sys_role_menu` VALUES (12, 1, 12);
INSERT INTO `sys_role_menu` VALUES (13, 1, 13);
INSERT INTO `sys_role_menu` VALUES (14, 1, 14);
INSERT INTO `sys_role_menu` VALUES (15, 1, 15);
INSERT INTO `sys_role_menu` VALUES (16, 1, 16);
INSERT INTO `sys_role_menu` VALUES (17, 1, 17);
INSERT INTO `sys_role_menu` VALUES (18, 1, 18);
INSERT INTO `sys_role_menu` VALUES (19, 1, 19);
INSERT INTO `sys_role_menu` VALUES (32, 1, 100);
INSERT INTO `sys_role_menu` VALUES (33, 1, 110);
INSERT INTO `sys_role_menu` VALUES (34, 1, 111);
INSERT INTO `sys_role_menu` VALUES (35, 1, 112);
INSERT INTO `sys_role_menu` VALUES (36, 1, 113);
INSERT INTO `sys_role_menu` VALUES (37, 1, 114);
INSERT INTO `sys_role_menu` VALUES (38, 1, 120);
INSERT INTO `sys_role_menu` VALUES (39, 1, 121);
INSERT INTO `sys_role_menu` VALUES (40, 1, 130);
INSERT INTO `sys_role_menu` VALUES (41, 1, 131);
INSERT INTO `sys_role_menu` VALUES (42, 1, 132);
INSERT INTO `sys_role_menu` VALUES (43, 1, 140);
INSERT INTO `sys_role_menu` VALUES (44, 1, 141);
INSERT INTO `sys_role_menu` VALUES (45, 1, 142);
INSERT INTO `sys_role_menu` VALUES (46, 1, 150);
INSERT INTO `sys_role_menu` VALUES (47, 1, 151);
INSERT INTO `sys_role_menu` VALUES (48, 1, 152);
INSERT INTO `sys_role_menu` VALUES (49, 1, 160);
INSERT INTO `sys_role_menu` VALUES (63, 100, 120);
INSERT INTO `sys_role_menu` VALUES (64, 100, 121);
INSERT INTO `sys_role_menu` VALUES (65, 101, 130);
INSERT INTO `sys_role_menu` VALUES (66, 101, 131);
INSERT INTO `sys_role_menu` VALUES (67, 101, 132);
INSERT INTO `sys_role_menu` VALUES (68, 102, 140);
INSERT INTO `sys_role_menu` VALUES (69, 102, 141);
INSERT INTO `sys_role_menu` VALUES (70, 102, 142);
INSERT INTO `sys_role_menu` VALUES (71, 103, 150);
INSERT INTO `sys_role_menu` VALUES (72, 103, 151);
INSERT INTO `sys_role_menu` VALUES (73, 103, 152);
INSERT INTO `sys_role_menu` VALUES (74, 104, 100);
INSERT INTO `sys_role_menu` VALUES (75, 104, 110);
INSERT INTO `sys_role_menu` VALUES (76, 104, 111);
INSERT INTO `sys_role_menu` VALUES (77, 104, 112);
INSERT INTO `sys_role_menu` VALUES (78, 104, 113);
INSERT INTO `sys_role_menu` VALUES (79, 104, 114);
INSERT INTO `sys_role_menu` VALUES (80, 105, 100);
INSERT INTO `sys_role_menu` VALUES (81, 105, 110);
INSERT INTO `sys_role_menu` VALUES (82, 105, 111);
INSERT INTO `sys_role_menu` VALUES (83, 105, 112);
INSERT INTO `sys_role_menu` VALUES (84, 105, 113);
INSERT INTO `sys_role_menu` VALUES (85, 105, 114);
INSERT INTO `sys_role_menu` VALUES (86, 105, 120);
INSERT INTO `sys_role_menu` VALUES (87, 105, 121);
INSERT INTO `sys_role_menu` VALUES (88, 105, 130);
INSERT INTO `sys_role_menu` VALUES (89, 105, 131);
INSERT INTO `sys_role_menu` VALUES (90, 105, 132);
INSERT INTO `sys_role_menu` VALUES (91, 105, 140);
INSERT INTO `sys_role_menu` VALUES (92, 105, 141);
INSERT INTO `sys_role_menu` VALUES (93, 105, 142);
INSERT INTO `sys_role_menu` VALUES (94, 105, 150);
INSERT INTO `sys_role_menu` VALUES (95, 105, 151);
INSERT INTO `sys_role_menu` VALUES (96, 105, 152);
INSERT INTO `sys_role_menu` VALUES (97, 105, 160);
INSERT INTO `sys_role_menu` VALUES (98, 1, 1000);
INSERT INTO `sys_role_menu` VALUES (99, 1, 1001);
INSERT INTO `sys_role_menu` VALUES (100, 1, 1002);
INSERT INTO `sys_role_menu` VALUES (101, 1, 1003);
INSERT INTO `sys_role_menu` VALUES (102, 1, 1010);
INSERT INTO `sys_role_menu` VALUES (103, 1, 1011);
INSERT INTO `sys_role_menu` VALUES (104, 1, 1012);
INSERT INTO `sys_role_menu` VALUES (105, 1, 1013);
INSERT INTO `sys_role_menu` VALUES (106, 1, 1020);
INSERT INTO `sys_role_menu` VALUES (107, 1, 1021);
INSERT INTO `sys_role_menu` VALUES (108, 1, 1022);
INSERT INTO `sys_role_menu` VALUES (109, 1, 1023);
INSERT INTO `sys_role_menu` VALUES (110, 1, 1030);
INSERT INTO `sys_role_menu` VALUES (111, 1, 1031);
INSERT INTO `sys_role_menu` VALUES (112, 1, 1032);
INSERT INTO `sys_role_menu` VALUES (113, 1, 1033);
INSERT INTO `sys_role_menu` VALUES (114, 1, 1040);
INSERT INTO `sys_role_menu` VALUES (115, 1, 1041);
INSERT INTO `sys_role_menu` VALUES (116, 1, 1042);
INSERT INTO `sys_role_menu` VALUES (117, 1, 1043);
INSERT INTO `sys_role_menu` VALUES (118, 1, 1050);
INSERT INTO `sys_role_menu` VALUES (119, 1, 1051);
INSERT INTO `sys_role_menu` VALUES (120, 1, 1052);
INSERT INTO `sys_role_menu` VALUES (121, 1, 1053);
INSERT INTO `sys_role_menu` VALUES (122, 1, 1060);
INSERT INTO `sys_role_menu` VALUES (123, 1, 1061);
INSERT INTO `sys_role_menu` VALUES (124, 1, 1062);
INSERT INTO `sys_role_menu` VALUES (125, 1, 1063);
INSERT INTO `sys_role_menu` VALUES (126, 1, 1070);
INSERT INTO `sys_role_menu` VALUES (127, 1, 1071);
INSERT INTO `sys_role_menu` VALUES (128, 1, 1072);
INSERT INTO `sys_role_menu` VALUES (129, 1, 1073);
INSERT INTO `sys_role_menu` VALUES (136, 1, 1074);
INSERT INTO `sys_role_menu` VALUES (130, 1, 1080);
INSERT INTO `sys_role_menu` VALUES (131, 1, 1081);
INSERT INTO `sys_role_menu` VALUES (132, 1, 1082);
INSERT INTO `sys_role_menu` VALUES (133, 1, 1083);
INSERT INTO `sys_role_menu` VALUES (134, 1, 1090);
INSERT INTO `sys_role_menu` VALUES (135, 1, 1091);
INSERT INTO `sys_role_menu` VALUES (161, 100, 1020);
INSERT INTO `sys_role_menu` VALUES (162, 100, 1021);
INSERT INTO `sys_role_menu` VALUES (163, 100, 1022);
INSERT INTO `sys_role_menu` VALUES (164, 100, 1023);
INSERT INTO `sys_role_menu` VALUES (168, 101, 1030);
INSERT INTO `sys_role_menu` VALUES (169, 101, 1031);
INSERT INTO `sys_role_menu` VALUES (170, 101, 1032);
INSERT INTO `sys_role_menu` VALUES (171, 101, 1033);
INSERT INTO `sys_role_menu` VALUES (172, 101, 1040);
INSERT INTO `sys_role_menu` VALUES (173, 101, 1041);
INSERT INTO `sys_role_menu` VALUES (174, 101, 1042);
INSERT INTO `sys_role_menu` VALUES (175, 101, 1043);
INSERT INTO `sys_role_menu` VALUES (183, 102, 1050);
INSERT INTO `sys_role_menu` VALUES (184, 102, 1051);
INSERT INTO `sys_role_menu` VALUES (185, 102, 1052);
INSERT INTO `sys_role_menu` VALUES (186, 102, 1053);
INSERT INTO `sys_role_menu` VALUES (187, 102, 1060);
INSERT INTO `sys_role_menu` VALUES (188, 102, 1061);
INSERT INTO `sys_role_menu` VALUES (189, 102, 1062);
INSERT INTO `sys_role_menu` VALUES (190, 102, 1063);
INSERT INTO `sys_role_menu` VALUES (198, 103, 1070);
INSERT INTO `sys_role_menu` VALUES (199, 103, 1071);
INSERT INTO `sys_role_menu` VALUES (200, 103, 1072);
INSERT INTO `sys_role_menu` VALUES (201, 103, 1073);
INSERT INTO `sys_role_menu` VALUES (206, 103, 1074);
INSERT INTO `sys_role_menu` VALUES (202, 103, 1080);
INSERT INTO `sys_role_menu` VALUES (203, 103, 1081);
INSERT INTO `sys_role_menu` VALUES (204, 103, 1082);
INSERT INTO `sys_role_menu` VALUES (205, 103, 1083);
INSERT INTO `sys_role_menu` VALUES (213, 104, 1000);
INSERT INTO `sys_role_menu` VALUES (214, 104, 1001);
INSERT INTO `sys_role_menu` VALUES (215, 104, 1002);
INSERT INTO `sys_role_menu` VALUES (216, 104, 1003);
INSERT INTO `sys_role_menu` VALUES (217, 104, 1010);
INSERT INTO `sys_role_menu` VALUES (218, 104, 1011);
INSERT INTO `sys_role_menu` VALUES (219, 104, 1012);
INSERT INTO `sys_role_menu` VALUES (220, 104, 1013);
INSERT INTO `sys_role_menu` VALUES (228, 105, 1000);
INSERT INTO `sys_role_menu` VALUES (229, 105, 1001);
INSERT INTO `sys_role_menu` VALUES (230, 105, 1002);
INSERT INTO `sys_role_menu` VALUES (231, 105, 1003);
INSERT INTO `sys_role_menu` VALUES (232, 105, 1010);
INSERT INTO `sys_role_menu` VALUES (233, 105, 1011);
INSERT INTO `sys_role_menu` VALUES (234, 105, 1012);
INSERT INTO `sys_role_menu` VALUES (235, 105, 1013);
INSERT INTO `sys_role_menu` VALUES (236, 105, 1020);
INSERT INTO `sys_role_menu` VALUES (237, 105, 1021);
INSERT INTO `sys_role_menu` VALUES (238, 105, 1022);
INSERT INTO `sys_role_menu` VALUES (239, 105, 1023);
INSERT INTO `sys_role_menu` VALUES (240, 105, 1030);
INSERT INTO `sys_role_menu` VALUES (241, 105, 1031);
INSERT INTO `sys_role_menu` VALUES (242, 105, 1032);
INSERT INTO `sys_role_menu` VALUES (243, 105, 1033);
INSERT INTO `sys_role_menu` VALUES (244, 105, 1040);
INSERT INTO `sys_role_menu` VALUES (245, 105, 1041);
INSERT INTO `sys_role_menu` VALUES (246, 105, 1042);
INSERT INTO `sys_role_menu` VALUES (247, 105, 1043);
INSERT INTO `sys_role_menu` VALUES (248, 105, 1050);
INSERT INTO `sys_role_menu` VALUES (249, 105, 1051);
INSERT INTO `sys_role_menu` VALUES (250, 105, 1052);
INSERT INTO `sys_role_menu` VALUES (251, 105, 1053);
INSERT INTO `sys_role_menu` VALUES (252, 105, 1060);
INSERT INTO `sys_role_menu` VALUES (253, 105, 1061);
INSERT INTO `sys_role_menu` VALUES (254, 105, 1062);
INSERT INTO `sys_role_menu` VALUES (255, 105, 1063);
INSERT INTO `sys_role_menu` VALUES (256, 105, 1070);
INSERT INTO `sys_role_menu` VALUES (257, 105, 1071);
INSERT INTO `sys_role_menu` VALUES (258, 105, 1072);
INSERT INTO `sys_role_menu` VALUES (259, 105, 1073);
INSERT INTO `sys_role_menu` VALUES (266, 105, 1074);
INSERT INTO `sys_role_menu` VALUES (260, 105, 1080);
INSERT INTO `sys_role_menu` VALUES (261, 105, 1081);
INSERT INTO `sys_role_menu` VALUES (262, 105, 1082);
INSERT INTO `sys_role_menu` VALUES (263, 105, 1083);
INSERT INTO `sys_role_menu` VALUES (264, 105, 1090);
INSERT INTO `sys_role_menu` VALUES (265, 105, 1091);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$dNdLO9pKrXD3Ns8OiZ283uRJ/bnXbFaXoUnZZv9cDZKr2S3sUuDly', '超级管理员', NULL, NULL, 1, NULL, '2026-06-23 23:01:14', '2026-06-25 20:37:12', 0);
INSERT INTO `sys_user` VALUES (2, 'test', '$2a$10$RT.q.ou2efILnObq1DJPquYVUb1VeEgmINRKm1WcH45kRqpRRSAnS', '测试用户', NULL, NULL, 1, NULL, '2026-07-06 20:37:02', '2026-07-06 20:37:02', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户-角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2, 101);

-- ----------------------------
-- Table structure for t_dispatch_list
-- ----------------------------
DROP TABLE IF EXISTS `t_dispatch_list`;
CREATE TABLE `t_dispatch_list`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dispatch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发车清单号',
  `list_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dispatch_no`(`dispatch_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '发车清单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_dispatch_list
-- ----------------------------
INSERT INTO `t_dispatch_list` VALUES (4, 'DLIST-2026-TEST-001', 'CONFIRMED', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_dispatch_list` VALUES (5, 'DLIST-2026-TEST-002', 'CONFIRMED', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_dispatch_list` VALUES (6, 'DLIST-2026-TEST-003', 'DRAFT', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_md_dealer
-- ----------------------------
DROP TABLE IF EXISTS `t_md_dealer`;
CREATE TABLE `t_md_dealer`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dealer_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '经销商编码',
  `dealer_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '经销商名称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dealer_code`(`dealer_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '经销商' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_md_dealer
-- ----------------------------
INSERT INTO `t_md_dealer` VALUES (7, 'DL001', 'Shanghai Pudong Experience Center', 1, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_md_dealer` VALUES (8, 'DL002', 'Beijing Chaoyang Flagship Store', 1, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_md_dealer` VALUES (9, 'DL003', 'Guangzhou Tianhe Delivery Center', 1, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_md_dealer` VALUES (10, 'DL004', 'Chengdu Gaoxin Experience Center', 1, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_md_dealer` VALUES (11, 'DL005', 'Hangzhou Binjiang Delivery Center', 1, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_md_dealer` VALUES (12, 'DL006', 'Shenzhen Nanshan Flagship Store', 1, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_md_exterior_color
-- ----------------------------
DROP TABLE IF EXISTS `t_md_exterior_color`;
CREATE TABLE `t_md_exterior_color`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `color_name_cn` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?????',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0?? 1??',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_md_exterior_color
-- ----------------------------
INSERT INTO `t_md_exterior_color` VALUES (6, 'Aurora Blue', '极光蓝', 1, '2026-07-06 22:10:41', '2026-07-06 22:18:01', 0);
INSERT INTO `t_md_exterior_color` VALUES (7, 'Moon White', '月光白', 1, '2026-07-06 22:10:41', '2026-07-06 22:18:10', 0);
INSERT INTO `t_md_exterior_color` VALUES (8, 'Graphite Gray', '石墨灰', 1, '2026-07-06 22:10:41', '2026-07-06 22:18:36', 0);
INSERT INTO `t_md_exterior_color` VALUES (9, 'Emerald Green', '翡翠绿', 1, '2026-07-06 22:10:41', '2026-07-06 22:18:54', 0);

-- ----------------------------
-- Table structure for t_md_interior_color
-- ----------------------------
DROP TABLE IF EXISTS `t_md_interior_color`;
CREATE TABLE `t_md_interior_color`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `color_name_cn` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?????',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0?? 1??',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_md_interior_color
-- ----------------------------
INSERT INTO `t_md_interior_color` VALUES (6, 'Midnight Black', '午夜黑', 1, '2026-07-06 22:10:41', '2026-07-06 22:21:51', 0);
INSERT INTO `t_md_interior_color` VALUES (7, 'Sand Beige', '沙棕', 1, '2026-07-06 22:10:41', '2026-07-06 22:21:51', 0);
INSERT INTO `t_md_interior_color` VALUES (8, 'Cloud Gray', '云灰', 1, '2026-07-06 22:10:41', '2026-07-06 22:21:51', 0);
INSERT INTO `t_md_interior_color` VALUES (9, 'Crimson Red', '绯红', 1, '2026-07-06 22:10:41', '2026-07-06 22:21:51', 0);

-- ----------------------------
-- Table structure for t_md_model
-- ----------------------------
DROP TABLE IF EXISTS `t_md_model`;
CREATE TABLE `t_md_model`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `series` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `model_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `year_make` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0?? 1??',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_md_model
-- ----------------------------
INSERT INTO `t_md_model` VALUES (6, 'MAT-A01-PRO', 'Alpha Pro 2026', 'Alpha', 'PRO', 'A01-PRO', '2026', 1, '2026-07-06 22:10:41', '2026-07-06 22:11:02', 0);
INSERT INTO `t_md_model` VALUES (7, 'MAT-B01-LUX', 'Beta Luxury 2026', 'Beta', 'LUX', 'B01-LUX', '2026', 1, '2026-07-06 22:10:41', '2026-07-06 22:11:02', 0);
INSERT INTO `t_md_model` VALUES (8, 'MAT-C01-SPT', 'Gamma Sport 2026', 'Gamma', 'SPORT', 'C01-SPORT', '2026', 1, '2026-07-06 22:10:41', '2026-07-06 22:11:02', 0);
INSERT INTO `t_md_model` VALUES (9, 'MAT-D01-EV', 'Delta EV 2026', 'Delta', 'EV', 'D01-EV', '2026', 1, '2026-07-06 22:10:41', '2026-07-06 22:11:02', 0);

-- ----------------------------
-- Table structure for t_sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_dict_item`;
CREATE TABLE `t_sys_dict_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_type_id` bigint NOT NULL COMMENT '所属字典类型ID',
  `item_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典值',
  `item_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典标签',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_type_id`(`dict_type_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典项' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_dict_item
-- ----------------------------
INSERT INTO `t_sys_dict_item` VALUES (27, 13, 'INVOICED', '正式发票', 1, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (28, 13, 'PROFORMA_INVOICED', '形式发票', 2, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (29, 14, 'PAID', '已收款', 1, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (30, 14, 'PAID_SINOSURE', '已收款-中信保', 2, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (31, 14, 'UNPAID', '未收款', 3, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (32, 15, 'DELIVERED', '已配送', 1, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (33, 15, 'IN_TRANSIT', '运输中', 2, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (34, 16, 'UPLOADED', '已上传', 1, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (35, 16, 'PENDING', '待上传', 2, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (36, 17, 'ALLOCATED', '已分配', 1, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_item` VALUES (37, 17, 'DELIVERED', '已交付', 2, 1, NULL, '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_dict_type`;
CREATE TABLE `t_sys_dict_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_code`(`dict_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_dict_type
-- ----------------------------
INSERT INTO `t_sys_dict_type` VALUES (13, 'invoice_status', '发票类型/状态', 1, '发票类型/状态', '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_type` VALUES (14, 'payment_status', '收款状态', 1, '收款状态', '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_type` VALUES (15, 'delivery_status', '配送状态', 1, '仓库到经销商配送状态', '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_type` VALUES (16, 'drosstech_status', '上牌状态', 1, '上牌资料上传状态', '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);
INSERT INTO `t_sys_dict_type` VALUES (17, 'sales_status', '销售状态', 1, '销售分配状态', '2026-07-06 22:01:14', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_transport_order
-- ----------------------------
DROP TABLE IF EXISTS `t_transport_order`;
CREATE TABLE `t_transport_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '运输单号',
  `date_to_storage_yard` date NULL DEFAULT NULL COMMENT '到仓库日期(整单共享)',
  `remark2` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注2',
  `order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
  `confirmed_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '运输单(车厂到仓库)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_transport_order
-- ----------------------------
INSERT INTO `t_transport_order` VALUES (5, 'TO-2026-TEST-001', '2026-06-06', 'Inbound batch 1', 'CONFIRMED', 'logistics_admin', '2026-06-06 10:30:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_transport_order` VALUES (6, 'TO-2026-TEST-002', '2026-06-10', 'Inbound batch 2', 'CONFIRMED', 'logistics_admin', '2026-06-10 10:30:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_transport_order` VALUES (7, 'TO-2026-TEST-003', NULL, 'Inbound draft batch', 'DRAFT', NULL, NULL, 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_transport_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_transport_order_item`;
CREATE TABLE `t_transport_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `transport_order_id` bigint NOT NULL COMMENT 'FK t_transport_order',
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `saic_buy_off_date` date NULL DEFAULT NULL COMMENT 'SAIC buy off日期(逐车)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_transport_order_id`(`transport_order_id` ASC) USING BTREE,
  INDEX `idx_vehicle_id`(`vehicle_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '运输单明细' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_transport_order_item
-- ----------------------------
INSERT INTO `t_transport_order_item` VALUES (13, 5, 14, '2026-06-02');
INSERT INTO `t_transport_order_item` VALUES (14, 5, 15, '2026-06-02');
INSERT INTO `t_transport_order_item` VALUES (15, 5, 16, '2026-06-03');
INSERT INTO `t_transport_order_item` VALUES (16, 5, 17, '2026-06-03');
INSERT INTO `t_transport_order_item` VALUES (17, 6, 18, '2026-06-05');
INSERT INTO `t_transport_order_item` VALUES (18, 6, 19, '2026-06-05');
INSERT INTO `t_transport_order_item` VALUES (19, 6, 20, '2026-06-06');
INSERT INTO `t_transport_order_item` VALUES (20, 6, 21, '2026-06-06');
INSERT INTO `t_transport_order_item` VALUES (21, 6, 22, '2026-06-07');
INSERT INTO `t_transport_order_item` VALUES (22, 6, 23, '2026-06-07');
INSERT INTO `t_transport_order_item` VALUES (23, 7, 24, NULL);

-- ----------------------------
-- Table structure for t_veh_allocation
-- ----------------------------
DROP TABLE IF EXISTS `t_veh_allocation`;
CREATE TABLE `t_veh_allocation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `stage_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT',
  `allocated_date` date NULL DEFAULT NULL COMMENT '分配日期',
  `dealer_id` bigint NULL DEFAULT NULL COMMENT 'FK t_md_dealer',
  `sales_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '销售状态(字典)',
  `remark3` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注3',
  `confirmed_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_vehicle_id`(`vehicle_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '销售分配阶段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_veh_allocation
-- ----------------------------
INSERT INTO `t_veh_allocation` VALUES (12, 14, 'CONFIRMED', '2026-06-02', 7, 'DELIVERED', 'Allocated sample DL001', 'sales_admin', '2026-06-05 14:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_allocation` VALUES (13, 15, 'CONFIRMED', '2026-06-03', 8, 'DELIVERED', 'Allocated sample DL002', 'sales_admin', '2026-06-05 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_allocation` VALUES (14, 16, 'CONFIRMED', '2026-06-04', 9, 'DELIVERED', 'Allocated sample DL003', 'sales_admin', '2026-06-06 14:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_allocation` VALUES (15, 17, 'CONFIRMED', '2026-06-05', 10, 'DELIVERED', 'Allocated sample DL004', 'sales_admin', '2026-06-06 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_allocation` VALUES (16, 18, 'CONFIRMED', '2026-06-06', 11, 'ALLOCATED', 'Allocated sample DL005', 'sales_admin', '2026-06-07 14:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_allocation` VALUES (17, 19, 'CONFIRMED', '2026-06-07', 12, 'ALLOCATED', 'Allocated sample DL006', 'sales_admin', '2026-06-07 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_allocation` VALUES (18, 20, 'CONFIRMED', '2026-06-08', 7, 'ALLOCATED', 'Allocated sample DL001 pending payment', 'sales_admin', '2026-06-08 14:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_allocation` VALUES (19, 21, 'CONFIRMED', '2026-06-09', 8, 'ALLOCATED', 'Allocated sample DL002 pending payment', 'sales_admin', '2026-06-08 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_allocation` VALUES (20, 22, 'CONFIRMED', '2026-06-10', 9, 'ALLOCATED', 'Allocated sample DL003 pending invoice', 'sales_admin', '2026-06-09 14:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_veh_delivery
-- ----------------------------
DROP TABLE IF EXISTS `t_veh_delivery`;
CREATE TABLE `t_veh_delivery` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `stage_status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `etd_to_dealer` date NULL DEFAULT NULL,
  `eta_to_dealer` date NULL DEFAULT NULL,
  `trolly_type` varchar(20) NULL DEFAULT NULL,
  `fully_load` tinyint NULL DEFAULT NULL,
  `received_date` date NULL DEFAULT NULL,
  `delivery_status` varchar(50) NULL DEFAULT NULL,
  `remark7` varchar(500) NULL DEFAULT NULL,
  `confirmed_by` varchar(50) NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehicle_id` (`vehicle_id`),
  KEY `idx_stage_status` (`stage_status`),
  KEY `idx_eta_to_dealer` (`eta_to_dealer`),
  KEY `idx_received_date` (`received_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '按 VIN 配送阶段';

-- ----------------------------
-- Table structure for t_veh_inbound
-- ----------------------------
DROP TABLE IF EXISTS `t_veh_inbound`;
CREATE TABLE `t_veh_inbound` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `stage_status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `saic_buy_off_date` date NULL DEFAULT NULL,
  `date_to_storage_yard` date NULL DEFAULT NULL,
  `remark2` varchar(500) NULL DEFAULT NULL,
  `confirmed_by` varchar(50) NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehicle_id` (`vehicle_id`),
  KEY `idx_stage_status` (`stage_status`),
  KEY `idx_storage_date` (`date_to_storage_yard`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='按 VIN 入库阶段';

-- ----------------------------
-- Table structure for t_veh_invoice
-- ----------------------------
DROP TABLE IF EXISTS `t_veh_invoice`;
CREATE TABLE `t_veh_invoice`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `stage_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT',
  `invoice_seq` int NOT NULL COMMENT '1=首次开票 2=转正记录',
  `invoice_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发票种类(正式/形式，字典)',
  `invoice_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发票号',
  `invoice_date` date NULL DEFAULT NULL COMMENT '发票日期',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注(remark4/remark6)',
  `confirmed_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_vehicle_id`(`vehicle_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '发票阶段(1:N)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_veh_invoice
-- ----------------------------
INSERT INTO `t_veh_invoice` VALUES (12, 14, 'CONFIRMED', 1, 'PROFORMA_INVOICED', 'PF-2026-TST-001', '2026-06-03', 'Proforma converted later', 'finance_admin', '2026-06-03 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (13, 14, 'CONFIRMED', 2, 'INVOICED', 'INV-2026-TST-001', '2026-06-08', 'Formal invoice after conversion', 'finance_admin', '2026-06-08 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (14, 15, 'CONFIRMED', 1, 'INVOICED', 'INV-2026-TST-002', '2026-06-09', 'Formal invoice sample', 'finance_admin', '2026-06-09 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (15, 16, 'CONFIRMED', 1, 'PROFORMA_INVOICED', 'PF-2026-TST-003', '2026-06-09', 'Pending registration proforma', 'finance_admin', '2026-06-09 12:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (16, 16, 'CONFIRMED', 2, 'INVOICED', 'INV-2026-TST-003', '2026-06-12', 'Pending registration formal', 'finance_admin', '2026-06-12 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (17, 17, 'CONFIRMED', 1, 'INVOICED', 'INV-2026-TST-004', '2026-06-13', 'Pending registration formal', 'finance_admin', '2026-06-13 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (18, 18, 'CONFIRMED', 1, 'INVOICED', 'INV-2026-TST-005', '2026-06-14', 'Pending delivery formal', 'finance_admin', '2026-06-14 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (19, 19, 'CONFIRMED', 1, 'INVOICED', 'INV-2026-TST-006', '2026-06-15', 'Pending delivery formal', 'finance_admin', '2026-06-15 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (20, 20, 'CONFIRMED', 1, 'INVOICED', 'INV-2026-TST-007', '2026-06-16', 'Pending payment formal', 'finance_admin', '2026-06-16 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (21, 21, 'CONFIRMED', 1, 'INVOICED', 'INV-2026-TST-008', '2026-06-17', 'Pending payment formal', 'finance_admin', '2026-06-17 11:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_invoice` VALUES (22, 22, 'DRAFT', 1, 'PROFORMA_INVOICED', 'PF-2026-TST-009', NULL, 'Draft invoice sample', NULL, NULL, 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_veh_payment
-- ----------------------------
DROP TABLE IF EXISTS `t_veh_payment`;
CREATE TABLE `t_veh_payment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `stage_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT',
  `payment_date` date NULL DEFAULT NULL COMMENT '收款日期',
  `credit_full_payment_date` date NULL DEFAULT NULL COMMENT '信用全款日期',
  `payment_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收款状态(字典)',
  `remark5` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注5',
  `confirmed_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_vehicle_id`(`vehicle_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收款阶段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_veh_payment
-- ----------------------------
INSERT INTO `t_veh_payment` VALUES (9, 14, 'CONFIRMED', '2026-06-09', '2026-06-10', 'PAID', 'Full payment received', 'finance_admin', '2026-06-10 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_payment` VALUES (10, 15, 'CONFIRMED', '2026-06-10', '2026-06-11', 'PAID_SINOSURE', 'Sinosure payment received', 'finance_admin', '2026-06-11 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_payment` VALUES (11, 16, 'CONFIRMED', '2026-06-13', '2026-06-14', 'PAID', 'Payment before registration', 'finance_admin', '2026-06-14 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_payment` VALUES (12, 17, 'CONFIRMED', '2026-06-14', '2026-06-15', 'PAID', 'Payment before registration', 'finance_admin', '2026-06-15 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_payment` VALUES (13, 18, 'CONFIRMED', '2026-06-15', '2026-06-16', 'PAID', 'Payment before delivery', 'finance_admin', '2026-06-16 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_payment` VALUES (14, 19, 'CONFIRMED', '2026-06-16', '2026-06-17', 'PAID_SINOSURE', 'Payment before delivery', 'finance_admin', '2026-06-17 15:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_payment` VALUES (15, 20, 'DRAFT', NULL, NULL, 'UNPAID', 'Pending payment draft', NULL, NULL, 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_payment` VALUES (16, 21, 'DRAFT', NULL, NULL, 'UNPAID', 'Pending payment draft', NULL, NULL, 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_veh_production
-- ----------------------------
DROP TABLE IF EXISTS `t_veh_production`;
CREATE TABLE `t_veh_production`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL,
  `stage_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `model_id` bigint NULL DEFAULT NULL,
  `year_make` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `material` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  `shipment` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Shipment',
  `batch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Batch',
  `offline_epmb_date` date NULL DEFAULT NULL COMMENT 'Offline EPMB??',
  `epmb_ok_date` date NULL DEFAULT NULL COMMENT 'EPMB ok??',
  `remark1` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??1',
  `confirmed_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `confirmed_at` datetime NULL DEFAULT NULL COMMENT '????',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `exterior_color_id` bigint NULL DEFAULT NULL,
  `interior_color_id` bigint NULL DEFAULT NULL,
  `engine_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_vehicle_id`(`vehicle_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_veh_production
-- ----------------------------
INSERT INTO `t_veh_production` VALUES (18, 14, 'CONFIRMED', 6, '2026', 'MAT-A01-PRO', 'SHP-2026-01', 'BATCH-2026-01', '2026-05-11', '2026-05-13', '??????-01', 'prod_admin', '2026-05-20 11:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 6, 6, 'ENG-TST-000001', 0);
INSERT INTO `t_veh_production` VALUES (19, 15, 'CONFIRMED', 7, '2026', 'MAT-B01-LUX', 'SHP-2026-01', 'BATCH-2026-01', '2026-05-12', '2026-05-14', '??????-02', 'prod_admin', '2026-05-20 12:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 7, 7, 'ENG-TST-000002', 0);
INSERT INTO `t_veh_production` VALUES (20, 16, 'CONFIRMED', 8, '2026', 'MAT-C01-SPT', 'SHP-2026-01', 'BATCH-2026-01', '2026-05-13', '2026-05-15', '??????-03', 'prod_admin', '2026-05-20 13:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 8, 9, 'ENG-TST-000003', 0);
INSERT INTO `t_veh_production` VALUES (21, 17, 'CONFIRMED', 6, '2026', 'MAT-A01-PRO', 'SHP-2026-01', 'BATCH-2026-02', '2026-05-14', '2026-05-16', '??????-04', 'prod_admin', '2026-05-20 14:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 9, 6, 'ENG-TST-000004', 0);
INSERT INTO `t_veh_production` VALUES (22, 18, 'CONFIRMED', 7, '2026', 'MAT-B01-LUX', 'SHP-2026-02', 'BATCH-2026-02', '2026-05-15', '2026-05-17', '??????-05', 'prod_admin', '2026-05-20 15:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 6, 8, 'ENG-TST-000005', 0);
INSERT INTO `t_veh_production` VALUES (23, 19, 'CONFIRMED', 8, '2026', 'MAT-C01-SPT', 'SHP-2026-02', 'BATCH-2026-02', '2026-05-16', '2026-05-18', '??????-06', 'prod_admin', '2026-05-20 16:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 7, 6, 'ENG-TST-000006', 0);
INSERT INTO `t_veh_production` VALUES (24, 20, 'CONFIRMED', 9, '2026', 'MAT-D01-EV', 'SHP-2026-02', 'BATCH-2026-03', '2026-05-17', '2026-05-19', '??????-07', 'prod_admin', '2026-05-20 17:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 8, 8, 'ENG-TST-000007', 0);
INSERT INTO `t_veh_production` VALUES (25, 21, 'CONFIRMED', 6, '2026', 'MAT-A01-PRO', 'SHP-2026-02', 'BATCH-2026-03', '2026-05-18', '2026-05-20', '??????-08', 'prod_admin', '2026-05-20 18:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 9, 7, 'ENG-TST-000008', 0);
INSERT INTO `t_veh_production` VALUES (26, 22, 'CONFIRMED', 7, '2026', 'MAT-B01-LUX', 'SHP-2026-03', 'BATCH-2026-03', '2026-05-19', '2026-05-21', '??????-09', 'prod_admin', '2026-05-20 19:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 6, 9, 'ENG-TST-000009', 0);
INSERT INTO `t_veh_production` VALUES (27, 23, 'CONFIRMED', 8, '2026', 'MAT-C01-SPT', 'SHP-2026-03', 'BATCH-2026-04', '2026-05-20', '2026-05-22', '??????-10', 'prod_admin', '2026-05-20 20:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 7, 8, 'ENG-TST-000010', 0);
INSERT INTO `t_veh_production` VALUES (28, 24, 'CONFIRMED', 9, '2026', 'MAT-D01-EV', 'SHP-2026-03', 'BATCH-2026-04', '2026-05-21', '2026-05-23', '??????-11', 'prod_admin', '2026-05-20 21:00:00', 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 8, 6, 'ENG-TST-000011', 0);
INSERT INTO `t_veh_production` VALUES (29, 25, 'DRAFT', 6, '2026', 'MAT-A01-PRO', 'SHP-2026-03', 'BATCH-2026-04', NULL, NULL, '??????-12', NULL, NULL, 'test_seed', '2026-07-06 22:10:41', 'test_seed', '2026-07-06 22:11:02', 9, 8, 'ENG-TST-000012', 0);

-- ----------------------------
-- Table structure for t_veh_registration
-- ----------------------------
DROP TABLE IF EXISTS `t_veh_registration`;
CREATE TABLE `t_veh_registration`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `stage_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT',
  `drosstech_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Drosstech状态(字典)',
  `upload_date` date NULL DEFAULT NULL COMMENT '上传日期',
  `registration_date` date NULL DEFAULT NULL COMMENT '注册日期',
  `customer_region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户区域',
  `remark8` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注8',
  `confirmed_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_vehicle_id`(`vehicle_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '上牌阶段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_veh_registration
-- ----------------------------
INSERT INTO `t_veh_registration` VALUES (5, 14, 'CONFIRMED', 'UPLOADED', '2026-06-18', '2026-06-22', 'East China 1', 'Registration completed', 'sales_admin', '2026-06-22 16:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_registration` VALUES (6, 15, 'CONFIRMED', 'UPLOADED', '2026-06-19', '2026-06-23', 'North China 1', 'Registration completed', 'sales_admin', '2026-06-23 16:00:00', 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_registration` VALUES (7, 16, 'DRAFT', 'PENDING', NULL, NULL, 'South China 1', 'Waiting for registration upload', NULL, NULL, 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);
INSERT INTO `t_veh_registration` VALUES (8, 17, 'DRAFT', 'PENDING', NULL, NULL, 'Southwest China 1', 'Waiting for registration upload', NULL, NULL, 'test_seed', '2026-07-06 22:01:14', 'test_seed', '2026-07-06 22:01:14', 0);

-- ----------------------------
-- Table structure for t_vehicle
-- ----------------------------
DROP TABLE IF EXISTS `t_vehicle`;
CREATE TABLE `t_vehicle`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vin` varchar(17) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '车辆识别码(VIN)',
  `lifecycle_stage` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '当前生命周期阶段',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_vin`(`vin` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '车辆主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_vehicle
-- ----------------------------
INSERT INTO `t_vehicle` VALUES (14, 'LSVTST26000000001', 'COMPLETED', 'test_seed', 'test_seed', '2026-06-01 09:00:00', '2026-06-22 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (15, 'LSVTST26000000002', 'COMPLETED', 'test_seed', 'test_seed', '2026-06-01 10:00:00', '2026-06-23 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (16, 'LSVTST26000000003', 'PENDING_REGISTRATION', 'test_seed', 'test_seed', '2026-06-02 09:00:00', '2026-06-18 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (17, 'LSVTST26000000004', 'PENDING_REGISTRATION', 'test_seed', 'test_seed', '2026-06-02 10:00:00', '2026-06-18 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (18, 'LSVTST26000000005', 'PENDING_DELIVERY', 'test_seed', 'test_seed', '2026-06-03 09:00:00', '2026-06-20 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (19, 'LSVTST26000000006', 'PENDING_DELIVERY', 'test_seed', 'test_seed', '2026-06-03 10:00:00', '2026-06-20 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (20, 'LSVTST26000000007', 'PENDING_PAYMENT', 'test_seed', 'test_seed', '2026-06-04 09:00:00', '2026-06-17 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (21, 'LSVTST26000000008', 'PENDING_PAYMENT', 'test_seed', 'test_seed', '2026-06-04 10:00:00', '2026-06-17 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (22, 'LSVTST26000000009', 'PENDING_INVOICE', 'test_seed', 'test_seed', '2026-06-05 09:00:00', '2026-06-16 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (23, 'LSVTST26000000010', 'PENDING_ALLOCATION', 'test_seed', 'test_seed', '2026-06-05 10:00:00', '2026-06-12 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (24, 'LSVTST26000000011', 'PENDING_INBOUND', 'test_seed', 'test_seed', '2026-06-06 09:00:00', '2026-06-08 18:00:00', 0);
INSERT INTO `t_vehicle` VALUES (25, 'LSVTST26000000012', 'PENDING_OFFLINE', 'test_seed', 'test_seed', '2026-06-06 10:00:00', '2026-06-06 18:00:00', 0);

-- ----------------------------
-- Table structure for t_waybill
-- ----------------------------
DROP TABLE IF EXISTS `t_waybill`;
CREATE TABLE `t_waybill`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `waybill_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路单号',
  `dispatch_list_id` bigint NOT NULL COMMENT 'FK t_dispatch_list',
  `trolly_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '轿运车类型(4 units/6 units)',
  `fully_load` tinyint NULL DEFAULT 0 COMMENT '是否满载',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dispatch_list_id`(`dispatch_list_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '行车路单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_waybill
-- ----------------------------
INSERT INTO `t_waybill` VALUES (4, 'WB-2026-TEST-001', 4, '4 units', 1);
INSERT INTO `t_waybill` VALUES (5, 'WB-2026-TEST-002', 5, '6 units', 0);
INSERT INTO `t_waybill` VALUES (6, 'WB-2026-TEST-003', 6, '4 units', 0);

-- ----------------------------
-- Table structure for t_waybill_dealer
-- ----------------------------
DROP TABLE IF EXISTS `t_waybill_dealer`;
CREATE TABLE `t_waybill_dealer`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `waybill_id` bigint NOT NULL COMMENT 'FK t_waybill',
  `dealer_id` bigint NOT NULL COMMENT 'FK t_md_dealer',
  `etd_to_dealer` date NULL DEFAULT NULL COMMENT '发车日期',
  `eta_to_dealer` date NULL DEFAULT NULL COMMENT '预计到达',
  `received_date` date NULL DEFAULT NULL COMMENT '签收日期',
  `delivery_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配送状态(字典)',
  `remark7` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注7',
  `row_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
  `confirmed_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_waybill_id`(`waybill_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '行车路单-经销商行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_waybill_dealer
-- ----------------------------
INSERT INTO `t_waybill_dealer` VALUES (7, 4, 8, '2026-06-11', '2026-06-14', '2026-06-14', 'DELIVERED', 'Beijing route received', 'CONFIRMED', 'logistics_admin', '2026-06-14 17:00:00');
INSERT INTO `t_waybill_dealer` VALUES (8, 4, 7, '2026-06-11', '2026-06-13', '2026-06-13', 'DELIVERED', 'Shanghai route received', 'CONFIRMED', 'logistics_admin', '2026-06-13 17:00:00');
INSERT INTO `t_waybill_dealer` VALUES (9, 5, 10, '2026-06-15', '2026-06-18', '2026-06-18', 'DELIVERED', 'Chengdu route received', 'CONFIRMED', 'logistics_admin', '2026-06-18 17:00:00');
INSERT INTO `t_waybill_dealer` VALUES (10, 5, 9, '2026-06-15', '2026-06-17', '2026-06-17', 'DELIVERED', 'Guangzhou route received', 'CONFIRMED', 'logistics_admin', '2026-06-17 17:00:00');
INSERT INTO `t_waybill_dealer` VALUES (11, 6, 12, '2026-06-20', '2026-06-22', NULL, 'IN_TRANSIT', 'Shenzhen route in transit', 'DRAFT', NULL, NULL);
INSERT INTO `t_waybill_dealer` VALUES (12, 6, 11, '2026-06-20', '2026-06-22', NULL, 'IN_TRANSIT', 'Hangzhou route in transit', 'DRAFT', NULL, NULL);

-- ----------------------------
-- Table structure for t_waybill_dealer_vin
-- ----------------------------
DROP TABLE IF EXISTS `t_waybill_dealer_vin`;
CREATE TABLE `t_waybill_dealer_vin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `waybill_dealer_id` bigint NOT NULL COMMENT 'FK t_waybill_dealer',
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_waybill_dealer_id`(`waybill_dealer_id` ASC) USING BTREE,
  INDEX `idx_vehicle_id`(`vehicle_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '经销商行-VIN' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_waybill_dealer_vin
-- ----------------------------
INSERT INTO `t_waybill_dealer_vin` VALUES (7, 7, 15);
INSERT INTO `t_waybill_dealer_vin` VALUES (8, 8, 14);
INSERT INTO `t_waybill_dealer_vin` VALUES (9, 9, 17);
INSERT INTO `t_waybill_dealer_vin` VALUES (10, 10, 16);
INSERT INTO `t_waybill_dealer_vin` VALUES (11, 11, 19);
INSERT INTO `t_waybill_dealer_vin` VALUES (12, 12, 18);

SET FOREIGN_KEY_CHECKS = 1;

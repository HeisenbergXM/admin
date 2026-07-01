-- ========================================================================
-- VLM 车辆全生命周期管理系统 - 初始化数据
-- 菜单、角色、字典种子数据
-- ========================================================================

USE admin_system;

-- ========== VLM 业务菜单（ID 从 100 起，避免与现有 sys_menu 冲突）==========

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, icon, sort_order) VALUES
-- 字典管理（归入系统管理）
(100, 1,  '字典管理',       2, '/system/dict',           'sys:dict:list', 'dict',       60),

-- 主数据管理
(110, 0,  '主数据管理',     1, '/master-data',           NULL, 'master',     100),
(111, 110,'车型数据',       2, '/master-data/model',     'sys:master:list', 'model',      101),
(112, 110,'外饰颜色',       2, '/master-data/exterior',  'sys:master:list', 'color',      102),
(113, 110,'内饰颜色',       2, '/master-data/interior',  'sys:master:list', 'color',      103),
(114, 110,'经销商维护',     2, '/master-data/dealer',    'sys:master:list', 'dealer',     104),

-- 生产管理
(120, 0,  '生产管理',       1, '/production',            NULL, 'production', 110),
(121, 120,'车辆录入',       2, '/production/vehicle',    'sys:vehicle:list', 'vehicle',    111),

-- 运输管理
(130, 0,  '运输管理',       1, '/transport',             NULL, 'transport',  120),
(131, 130,'车厂到仓库',     2, '/transport/inbound',     'vlm:transport:list', 'inbound',    121),
(132, 130,'仓库到经销商',   2, '/transport/outbound',    'vlm:dispatch:list', 'outbound',   122),

-- 销售管理
(140, 0,  '销售管理',       1, '/sales',                 NULL, 'sales',      130),
(141, 140,'车辆销售',       2, '/sales/allocation',      'vlm:allocation:list', 'allocation', 131),
(142, 140,'车辆上牌',       2, '/sales/registration',    'vlm:registration:list', 'registration',132),

-- 财务管理
(150, 0,  '财务管理',       1, '/finance',               NULL, 'finance',    140),
(151, 150,'发票确认',       2, '/finance/invoice',       'vlm:invoice:list', 'invoice',    141),
(152, 150,'收款确认',       2, '/finance/payment',       'vlm:payment:list', 'payment',    142),

-- 车辆全景视图
(160, 0,  '车辆全景视图',   2, '/panorama',              'vlm:panorama:view', 'panorama',   150),

-- 字典管理按钮
(1000, 100, '字典查询',       3, NULL, 'sys:dict:list',          NULL, 1000),
(1001, 100, '字典新增',       3, NULL, 'sys:dict:add',           NULL, 1001),
(1002, 100, '字典编辑',       3, NULL, 'sys:dict:edit',          NULL, 1002),
(1003, 100, '字典删除',       3, NULL, 'sys:dict:delete',        NULL, 1003),

-- 主数据按钮
(1010, 110, '主数据查询',     3, NULL, 'sys:master:list',        NULL, 1010),
(1011, 110, '主数据新增',     3, NULL, 'sys:master:add',         NULL, 1011),
(1012, 110, '主数据编辑',     3, NULL, 'sys:master:edit',        NULL, 1012),
(1013, 110, '主数据删除',     3, NULL, 'sys:master:delete',      NULL, 1013),

-- 车辆生产按钮
(1020, 121, '车辆查询',       3, NULL, 'sys:vehicle:list',       NULL, 1020),
(1021, 121, '车辆新增',       3, NULL, 'sys:vehicle:add',        NULL, 1021),
(1022, 121, '车辆编辑',       3, NULL, 'sys:vehicle:edit',       NULL, 1022),
(1023, 121, '车辆确认',       3, NULL, 'sys:vehicle:confirm',    NULL, 1023),

-- 运输单按钮
(1030, 131, '运输单查询',     3, NULL, 'vlm:transport:list',     NULL, 1030),
(1031, 131, '运输单新增',     3, NULL, 'vlm:transport:add',      NULL, 1031),
(1032, 131, '运输单编辑',     3, NULL, 'vlm:transport:edit',     NULL, 1032),
(1033, 131, '运输单确认',     3, NULL, 'vlm:transport:confirm',  NULL, 1033),

-- 发车清单按钮
(1040, 132, '发车清单查询',   3, NULL, 'vlm:dispatch:list',      NULL, 1040),
(1041, 132, '发车清单新增',   3, NULL, 'vlm:dispatch:add',       NULL, 1041),
(1042, 132, '发车清单编辑',   3, NULL, 'vlm:dispatch:edit',      NULL, 1042),
(1043, 132, '发车清单确认',   3, NULL, 'vlm:dispatch:confirm',   NULL, 1043),

-- 销售分配按钮
(1050, 141, '销售分配查询',   3, NULL, 'vlm:allocation:list',    NULL, 1050),
(1051, 141, '销售分配新增',   3, NULL, 'vlm:allocation:add',     NULL, 1051),
(1052, 141, '销售分配编辑',   3, NULL, 'vlm:allocation:edit',    NULL, 1052),
(1053, 141, '销售分配确认',   3, NULL, 'vlm:allocation:confirm', NULL, 1053),

-- 上牌按钮
(1060, 142, '上牌查询',       3, NULL, 'vlm:registration:list',  NULL, 1060),
(1061, 142, '上牌新增',       3, NULL, 'vlm:registration:add',   NULL, 1061),
(1062, 142, '上牌编辑',       3, NULL, 'vlm:registration:edit',  NULL, 1062),
(1063, 142, '上牌确认',       3, NULL, 'vlm:registration:confirm', NULL, 1063),

-- 发票按钮
(1070, 151, '发票查询',       3, NULL, 'vlm:invoice:list',       NULL, 1070),
(1071, 151, '发票新增',       3, NULL, 'vlm:invoice:add',        NULL, 1071),
(1072, 151, '发票编辑',       3, NULL, 'vlm:invoice:edit',       NULL, 1072),
(1073, 151, '形式发票转正',   3, NULL, 'vlm:invoice:convert',    NULL, 1073),

-- 收款按钮
(1080, 152, '收款查询',       3, NULL, 'vlm:payment:list',       NULL, 1080),
(1081, 152, '收款新增',       3, NULL, 'vlm:payment:add',        NULL, 1081),
(1082, 152, '收款编辑',       3, NULL, 'vlm:payment:edit',       NULL, 1082),
(1083, 152, '收款确认',       3, NULL, 'vlm:payment:confirm',    NULL, 1083),

-- 全景按钮
(1090, 160, '全景查看',       3, NULL, 'vlm:panorama:view',      NULL, 1090),
(1091, 160, '全景导出',       3, NULL, 'vlm:panorama:export',    NULL, 1091);

-- ADMIN 角色自动获得 VLM 所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id >= 100;

-- ========== VLM 业务角色 ==========

INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(100, '生产专员',     'PROD_SPECIALIST',     '车辆生产数据录入',             1),
(101, '物流专员',     'LOGISTICS_SPECIALIST','车厂到仓库、仓库到经销商配送',   1),
(102, '销售专员',     'SALES_SPECIALIST',    '车辆销售分配、车辆上牌',         1),
(103, '财务专员',     'FINANCE_SPECIALIST',  '发票确认、收款确认、形式发票转正',1),
(104, '主数据管理员', 'MASTER_DATA_ADMIN',   '车型/颜色/经销商主数据维护',     1),
(105, '业务主管',     'BUSINESS_MANAGER',    '全局查看、车辆全景视图',         1);

-- 角色-菜单授权（默认建议，最终可调整）

-- 生产专员：生产管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(100, 120), (100, 121),
(100, 1020), (100, 1021), (100, 1022), (100, 1023);

-- 物流专员：运输管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(101, 130), (101, 131), (101, 132),
(101, 1030), (101, 1031), (101, 1032), (101, 1033),
(101, 1040), (101, 1041), (101, 1042), (101, 1043);

-- 销售专员：销售管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(102, 140), (102, 141), (102, 142),
(102, 1050), (102, 1051), (102, 1052), (102, 1053),
(102, 1060), (102, 1061), (102, 1062), (102, 1063);

-- 财务专员：财务管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(103, 150), (103, 151), (103, 152),
(103, 1070), (103, 1071), (103, 1072), (103, 1073),
(103, 1080), (103, 1081), (103, 1082), (103, 1083);

-- 主数据管理员：主数据管理 + 字典管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(104, 100),
(104, 1000), (104, 1001), (104, 1002), (104, 1003),
(104, 110), (104, 111), (104, 112), (104, 113), (104, 114),
(104, 1010), (104, 1011), (104, 1012), (104, 1013);

-- 业务主管：全部模块（只读）+ 车辆全景视图
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 105, id FROM sys_menu WHERE id >= 100;

-- ========== 字典初始数据 ==========

-- 发票状态
INSERT INTO t_sys_dict_type (id, dict_code, dict_name, remark) VALUES
(1, 'invoice_status', '发票状态', '发票种类');

INSERT INTO t_sys_dict_item (dict_type_id, item_value, item_label, sort_order) VALUES
(1, 'INVOICED',          '正式发票 Invoice',       1),
(1, 'PROFORMA_INVOICED', '形式发票 Proforma',      2);

-- 收款状态
INSERT INTO t_sys_dict_type (id, dict_code, dict_name, remark) VALUES
(2, 'payment_status', '收款状态', 'Payment Status');

INSERT INTO t_sys_dict_item (dict_type_id, item_value, item_label, sort_order) VALUES
(2, 'PAID',           'Paid',               1),
(2, 'PAID_SINOSURE',  'Paid-Sinosure',      2),
(2, 'UNPAID',         'Unpaid',             3);

-- 配送状态
INSERT INTO t_sys_dict_type (id, dict_code, dict_name, remark) VALUES
(3, 'delivery_status', '配送状态', 'Delivery Status');

INSERT INTO t_sys_dict_item (dict_type_id, item_value, item_label, sort_order) VALUES
(3, 'DELIVERED',  'Delivered',  1),
(3, 'IN_TRANSIT', 'In Transit', 2);

-- Drosstech 上牌状态
INSERT INTO t_sys_dict_type (id, dict_code, dict_name, remark) VALUES
(4, 'drosstech_status', '上牌状态', 'Drosstech Status');

INSERT INTO t_sys_dict_item (dict_type_id, item_value, item_label, sort_order) VALUES
(4, 'UPLOADED', 'Uploaded', 1),
(4, 'PENDING',  'Pending',  2);

-- 销售状态
INSERT INTO t_sys_dict_type (id, dict_code, dict_name, remark) VALUES
(5, 'sales_status', '销售状态', 'Remark3 / Sales Status');

INSERT INTO t_sys_dict_item (dict_type_id, item_value, item_label, sort_order) VALUES
(5, 'ALLOCATED', 'Allocated', 1),
(5, 'DELIVERED', 'Delivered', 2);

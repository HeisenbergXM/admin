-- ========================================================================
-- VLM 车辆全生命周期管理系统 - 初始化数据
-- 菜单、角色、字典种子数据
-- ========================================================================

USE admin_system;

-- ========== VLM 业务菜单（ID 从 100 起，避免与现有 sys_menu 冲突）==========

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, icon, sort_order) VALUES
-- 字典管理（归入系统管理）
(100, 1,  '字典管理',       2, '/system/dict',           NULL, 'dict',       60),

-- 主数据管理
(110, 0,  '主数据管理',     1, '/master-data',           NULL, 'master',     100),
(111, 110,'车型数据',       2, '/master-data/model',     NULL, 'model',      101),
(112, 110,'外饰颜色',       2, '/master-data/exterior',  NULL, 'color',      102),
(113, 110,'内饰颜色',       2, '/master-data/interior',  NULL, 'color',      103),
(114, 110,'经销商维护',     2, '/master-data/dealer',    NULL, 'dealer',     104),

-- 生产管理
(120, 0,  '生产管理',       1, '/production',            NULL, 'production', 110),
(121, 120,'车辆录入',       2, '/production/vehicle',    NULL, 'vehicle',    111),

-- 运输管理
(130, 0,  '运输管理',       1, '/transport',             NULL, 'transport',  120),
(131, 130,'车厂到仓库',     2, '/transport/inbound',     NULL, 'inbound',    121),
(132, 130,'仓库到经销商',   2, '/transport/outbound',    NULL, 'outbound',   122),

-- 销售管理
(140, 0,  '销售管理',       1, '/sales',                 NULL, 'sales',      130),
(141, 140,'车辆销售',       2, '/sales/allocation',      NULL, 'allocation', 131),
(142, 140,'车辆上牌',       2, '/sales/registration',    NULL, 'registration',132),

-- 财务管理
(150, 0,  '财务管理',       1, '/finance',               NULL, 'finance',    140),
(151, 150,'发票确认',       2, '/finance/invoice',       NULL, 'invoice',    141),
(152, 150,'收款确认',       2, '/finance/payment',       NULL, 'payment',    142),

-- 车辆全景视图
(160, 0,  '车辆全景视图',   2, '/panorama',              NULL, 'panorama',   150);

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
(100, 120), (100, 121);

-- 物流专员：运输管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(101, 130), (101, 131), (101, 132);

-- 销售专员：销售管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(102, 140), (102, 141), (102, 142);

-- 财务专员：财务管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(103, 150), (103, 151), (103, 152);

-- 主数据管理员：主数据管理 + 字典管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(104, 100),
(104, 110), (104, 111), (104, 112), (104, 113), (104, 114);

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

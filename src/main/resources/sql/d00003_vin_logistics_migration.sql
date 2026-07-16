CREATE TABLE IF NOT EXISTS `t_veh_inbound` (
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

CREATE TABLE IF NOT EXISTS `t_veh_delivery` (
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

INSERT INTO `sys_menu`
  (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `permission`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
  (133, 130, '中转运输', 2, '/transport/vin-inbound', 'vlm:inbound:list', 'inbound', 123, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (134, 130, '发车清单', 2, '/transport/vin-delivery', 'vlm:delivery:list', 'outbound', 124, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1100, 133, '入库查询', 3, NULL, 'vlm:inbound:list', NULL, 1100, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1101, 133, '入库新增', 3, NULL, 'vlm:inbound:add', NULL, 1101, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1102, 133, '入库编辑', 3, NULL, 'vlm:inbound:edit', NULL, 1102, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1103, 133, '入库确认', 3, NULL, 'vlm:inbound:confirm', NULL, 1103, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1110, 134, '配送查询', 3, NULL, 'vlm:delivery:list', NULL, 1110, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1111, 134, '配送新增', 3, NULL, 'vlm:delivery:add', NULL, 1111, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1112, 134, '配送编辑', 3, NULL, 'vlm:delivery:edit', NULL, 1112, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1113, 134, '配送确认', 3, NULL, 'vlm:delivery:confirm', NULL, 1113, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `menu_name` = VALUES(`menu_name`),
  `menu_type` = VALUES(`menu_type`),
  `path` = VALUES(`path`),
  `permission` = VALUES(`permission`),
  `icon` = VALUES(`icon`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `update_time` = CURRENT_TIMESTAMP,
  `deleted` = VALUES(`deleted`);

UPDATE `sys_menu` SET `status` = 0 WHERE `id` IN (131, 132);
UPDATE `sys_menu` SET `status` = 1
WHERE `id` IN (1030,1031,1032,1033,1040,1041,1042,1043);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 267, 1, 133 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 133);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 268, 1, 134 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 134);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 269, 1, 1100 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1100);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 270, 1, 1101 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1101);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 271, 1, 1102 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1102);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 272, 1, 1103 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1103);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 273, 1, 1110 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1110);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 274, 1, 1111 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1111);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 275, 1, 1112 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1112);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 276, 1, 1113 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1113);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 277, 101, 133 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 133);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 278, 101, 134 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 134);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 279, 101, 1100 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 1100);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 280, 101, 1101 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 1101);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 281, 101, 1102 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 1102);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 282, 101, 1103 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 1103);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 283, 101, 1110 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 1110);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 284, 101, 1111 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 1111);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 285, 101, 1112 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 1112);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 286, 101, 1113 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = 1113);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 287, 105, 133 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 105 AND `menu_id` = 133);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 288, 105, 134 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 105 AND `menu_id` = 134);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 289, 105, 1100 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 105 AND `menu_id` = 1100);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT 290, 105, 1110 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 105 AND `menu_id` = 1110);

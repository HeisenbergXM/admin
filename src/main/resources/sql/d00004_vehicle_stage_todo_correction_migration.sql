INSERT INTO `sys_menu`
  (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `permission`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
  (122, 120, '车辆数据修订', 2, '/production/vehicle-correction', 'vlm:vehicle-correction:list', 'edit', 112, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1120, 122, '车辆修订查询', 3, NULL, 'vlm:vehicle-correction:list', NULL, 1120, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
  (1121, 122, '车辆修订编辑', 3, NULL, 'vlm:vehicle-correction:edit', NULL, 1121, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
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

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 122
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 122
);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 1120
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1120
);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 1121
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1121
);

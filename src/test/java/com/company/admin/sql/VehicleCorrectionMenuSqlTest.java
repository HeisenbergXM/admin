package com.company.admin.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleCorrectionMenuSqlTest {

    @Test
    void fullSeedDefinesAdminOnlyCorrectionMenu() throws IOException {
        String sql = resource("/sql/admin_system.sql");

        assertTrue(sql.contains("(122, 120, '车辆数据修订', 2, '/production/vehicle-correction', 'vlm:vehicle-correction:list'"));
        assertTrue(sql.contains("(1120, 122, '车辆修订查询', 3, NULL, 'vlm:vehicle-correction:list'"));
        assertTrue(sql.contains("(1121, 122, '车辆修订编辑', 3, NULL, 'vlm:vehicle-correction:edit'"));
        assertTrue(sql.contains("(1122, 122, '车辆修订导出', 3, NULL, 'vlm:vehicle-correction:export'"));
        assertTrue(sql.contains("(291, 1, 122)"));
        assertTrue(sql.contains("(292, 1, 1120)"));
        assertTrue(sql.contains("(293, 1, 1121)"));
        assertTrue(sql.contains("(294, 1, 1122)"));
        assertFalse(Pattern.compile("VALUES \\(\\d+, (100|101|102|103|104|105), (122|1120|1121|1122)\\)")
                .matcher(sql).find());
    }

    @Test
    void migrationUsesIdempotentRoleAssignmentsWithoutFixedJoinIds() throws IOException {
        String sql = resource("/sql/d00004_vehicle_stage_todo_correction_migration.sql");

        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(sql.contains("(1122, 122, '车辆修订导出', 3, NULL, 'vlm:vehicle-correction:export'"));
        assertTrue(sql.contains("INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)"));
        assertTrue(sql.contains("WHERE NOT EXISTS"));
        assertTrue(sql.contains("SELECT 1, 1122\nWHERE NOT EXISTS (\n    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1122\n);"));
        assertFalse(sql.contains("INSERT INTO `sys_role_menu` (`id`"));
    }

    private String resource(String path) throws IOException {
        InputStream stream = getClass().getResourceAsStream(path);
        assertNotNull(stream, "Missing SQL resource: " + path);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}

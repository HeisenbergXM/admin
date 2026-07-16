package com.company.admin.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VlmMenuSeedDataTest {

    @Test
    void pageMenusHavePermissionCodes() throws IOException {
        String sql = seedSql();

        Pattern menuRow = Pattern.compile(
                "\\((\\d+),\\s*\\d+,\\s*'([^']+)',\\s*(\\d+),\\s*'[^']*',\\s*(NULL|'[^']+'),",
                Pattern.MULTILINE);

        Matcher matcher = menuRow.matcher(sql);
        List<String> missingPermissions = new ArrayList<>();
        while (matcher.find()) {
            int menuType = Integer.parseInt(matcher.group(3));
            String permission = matcher.group(4);
            if (menuType == 2 && "NULL".equals(permission)) {
                missingPermissions.add(matcher.group(1) + ":" + matcher.group(2));
            }
        }

        assertTrue(missingPermissions.isEmpty(),
                "VLM page menus missing permission codes: " + missingPermissions);
    }

    @Test
    void seedHidesLegacyPagesButKeepsLegacyApiPermissionsEnabled() throws IOException {
        String sql = seedSql();
        assertTrue(sql.contains("(131, 130, '车厂到仓库', 2, '/transport/inbound', 'vlm:transport:list', 'inbound', 121, 0,"));
        assertTrue(sql.contains("(132, 130, '仓库到经销商', 2, '/transport/outbound', 'vlm:dispatch:list', 'outbound', 122, 0,"));
        assertTrue(sql.contains("(1030, 131, '运输单查询', 3, NULL, 'vlm:transport:list', NULL, 1030, 1,"));
        assertTrue(sql.contains("(1043, 132, '发车清单确认', 3, NULL, 'vlm:dispatch:confirm', NULL, 1043, 1,"));
    }

    @Test
    void seedDefinesNewVinLogisticsMenusAndPermissions() throws IOException {
        String sql = seedSql();
        assertTrue(sql.contains("'中转运输'"));
        assertTrue(sql.contains("'/transport/vin-inbound'"));
        assertTrue(sql.contains("'vlm:inbound:confirm'"));
        assertTrue(sql.contains("'/transport/vin-delivery'"));
        assertTrue(sql.contains("'vlm:delivery:confirm'"));
    }

    @Test
    void seedAssignsNewVinLogisticsPermissionsByRole() throws IOException {
        String sql = seedSql();
        Set<Integer> newMenuIds = Set.of(133, 134, 1100, 1101, 1102, 1103, 1110, 1111, 1112, 1113);

        assertEquals(newMenuIds, newRoleMenuIds(sql, 101, newMenuIds));
        assertEquals(Set.of(133, 134, 1100, 1110), newRoleMenuIds(sql, 105, newMenuIds));
    }

    private Set<Integer> newRoleMenuIds(String sql, int roleId, Set<Integer> newMenuIds) {
        Pattern roleMenuRow = Pattern.compile(
                "INSERT INTO `sys_role_menu` VALUES \\(\\d+,\\s*" + roleId + ",\\s*(\\d+)\\);",
                Pattern.MULTILINE);
        Matcher matcher = roleMenuRow.matcher(sql);
        Set<Integer> assignedIds = new HashSet<>();
        while (matcher.find()) {
            int menuId = Integer.parseInt(matcher.group(1));
            if (newMenuIds.contains(menuId)) {
                assignedIds.add(menuId);
            }
        }
        return assignedIds;
    }

    private String seedSql() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/sql/admin_system.sql");
        assertNotNull(stream, "Missing SQL seed resource: /sql/admin_system.sql");
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}

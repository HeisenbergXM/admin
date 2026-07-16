package com.company.admin.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VlmMenuSeedDataTest {

    private static final List<Integer> NEW_MENU_IDS =
            List.of(133, 134, 1100, 1101, 1102, 1103, 1110, 1111, 1112, 1113);
    private static final Pattern MENU_ROW = Pattern.compile(
            "\\((\\d+),\\s*(\\d+),\\s*'([^']+)',\\s*(\\d+),\\s*(NULL|'[^']*'),"
                    + "\\s*(NULL|'[^']*'),\\s*(NULL|'[^']*'),\\s*(\\d+),\\s*(\\d+),",
            Pattern.MULTILINE);
    private static final Pattern SEED_ROLE_MENU_ROW = Pattern.compile(
            "INSERT INTO `sys_role_menu` VALUES \\((\\d+),\\s*(\\d+),\\s*(\\d+)\\);",
            Pattern.MULTILINE);
    private static final Pattern MIGRATION_ROLE_MENU_ROW = Pattern.compile(
            "SELECT\\s+(\\d+),\\s*(\\d+),\\s*(\\d+)\\s+WHERE NOT EXISTS\\s*"
                    + "\\(SELECT 1 FROM `sys_role_menu` WHERE `role_id` = (\\d+) AND `menu_id` = (\\d+)\\);",
            Pattern.MULTILINE);

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
        assertMenuRowsExactly(sql, List.of(
                menu(131, 130, "车厂到仓库", 2, "/transport/inbound", "vlm:transport:list", "inbound", 121, 0),
                menu(132, 130, "仓库到经销商", 2, "/transport/outbound", "vlm:dispatch:list", "outbound", 122, 0),
                menu(1030, 131, "运输单查询", 3, null, "vlm:transport:list", null, 1030, 1),
                menu(1031, 131, "运输单新增", 3, null, "vlm:transport:add", null, 1031, 1),
                menu(1032, 131, "运输单编辑", 3, null, "vlm:transport:edit", null, 1032, 1),
                menu(1033, 131, "运输单确认", 3, null, "vlm:transport:confirm", null, 1033, 1),
                menu(1040, 132, "发车清单查询", 3, null, "vlm:dispatch:list", null, 1040, 1),
                menu(1041, 132, "发车清单新增", 3, null, "vlm:dispatch:add", null, 1041, 1),
                menu(1042, 132, "发车清单编辑", 3, null, "vlm:dispatch:edit", null, 1042, 1),
                menu(1043, 132, "发车清单确认", 3, null, "vlm:dispatch:confirm", null, 1043, 1)
        ));
    }

    @Test
    void seedDefinesNewVinLogisticsMenusAndPermissions() throws IOException {
        assertMenuRowsExactly(seedSql(), expectedNewMenuRows());
    }

    @Test
    void seedAssignsNewVinLogisticsPermissionsByRole() throws IOException {
        List<RoleMenuRow> allRows = seedRoleMenuRows(seedSql());
        List<RoleMenuRow> expectedRows = expectedRoleMenuRows();
        List<RoleMenuRow> fixedIdRows = allRows.stream()
                .filter(row -> row.id() >= 267 && row.id() <= 290)
                .toList();

        assertEquals(expectedRows, fixedIdRows, "sys_role_menu IDs 267-290 must match the role contract");
        for (RoleMenuRow expected : expectedRows) {
            long duplicateCount = allRows.stream()
                    .filter(row -> row.roleId() == expected.roleId() && row.menuId() == expected.menuId())
                    .count();
            assertEquals(1L, duplicateCount,
                    "Expected exactly one mapping for role=" + expected.roleId() + ", menu=" + expected.menuId());
        }

        assertEquals(NEW_MENU_IDS, newMenuAssignments(allRows, 1));
        assertEquals(NEW_MENU_IDS, newMenuAssignments(allRows, 101));
        assertEquals(List.of(133, 134, 1100, 1110), newMenuAssignments(allRows, 105));
    }

    @Test
    void migrationMatchesSeedMenuAndRoleContractAndRemainsIdempotent() throws IOException {
        String seed = seedSql();
        String migration = migrationSql();
        List<MenuRow> expectedMenus = expectedNewMenuRows();
        List<RoleMenuRow> expectedMappings = expectedRoleMenuRows();

        assertMenuRowsExactly(migration, expectedMenus);
        assertEquals(menuRowsForIds(seed, NEW_MENU_IDS), menuRowsForIds(migration, NEW_MENU_IDS),
                "Migration and full seed must define identical VIN menu business fields");

        List<RoleMenuRow> migrationMappings = migrationRoleMenuRows(migration);
        assertEquals(expectedMappings, migrationMappings,
                "Migration must use the same fixed role-menu IDs as the full seed");
        assertEquals(24, countOccurrences(migration, "WHERE NOT EXISTS"));
        assertTrue(migration.contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(migration.contains("UPDATE `sys_menu` SET `status` = 0 WHERE `id` IN (131, 132);"));
        assertTrue(migration.contains("WHERE `id` IN (1030,1031,1032,1033,1040,1041,1042,1043);"));
        assertFalse(Pattern.compile("DELETE\\s+FROM\\s+`sys_role_menu`", Pattern.CASE_INSENSITIVE)
                .matcher(migration).find(), "Migration must not delete legacy role-menu mappings");
    }

    private List<MenuRow> expectedNewMenuRows() {
        return List.of(
                menu(133, 130, "中转运输", 2, "/transport/vin-inbound", "vlm:inbound:list", "inbound", 123, 1),
                menu(134, 130, "发车清单", 2, "/transport/vin-delivery", "vlm:delivery:list", "outbound", 124, 1),
                menu(1100, 133, "入库查询", 3, null, "vlm:inbound:list", null, 1100, 1),
                menu(1101, 133, "入库新增", 3, null, "vlm:inbound:add", null, 1101, 1),
                menu(1102, 133, "入库编辑", 3, null, "vlm:inbound:edit", null, 1102, 1),
                menu(1103, 133, "入库确认", 3, null, "vlm:inbound:confirm", null, 1103, 1),
                menu(1110, 134, "配送查询", 3, null, "vlm:delivery:list", null, 1110, 1),
                menu(1111, 134, "配送新增", 3, null, "vlm:delivery:add", null, 1111, 1),
                menu(1112, 134, "配送编辑", 3, null, "vlm:delivery:edit", null, 1112, 1),
                menu(1113, 134, "配送确认", 3, null, "vlm:delivery:confirm", null, 1113, 1)
        );
    }

    private List<RoleMenuRow> expectedRoleMenuRows() {
        return List.of(
                roleMenu(267, 1, 133), roleMenu(268, 1, 134),
                roleMenu(269, 1, 1100), roleMenu(270, 1, 1101),
                roleMenu(271, 1, 1102), roleMenu(272, 1, 1103),
                roleMenu(273, 1, 1110), roleMenu(274, 1, 1111),
                roleMenu(275, 1, 1112), roleMenu(276, 1, 1113),
                roleMenu(277, 101, 133), roleMenu(278, 101, 134),
                roleMenu(279, 101, 1100), roleMenu(280, 101, 1101),
                roleMenu(281, 101, 1102), roleMenu(282, 101, 1103),
                roleMenu(283, 101, 1110), roleMenu(284, 101, 1111),
                roleMenu(285, 101, 1112), roleMenu(286, 101, 1113),
                roleMenu(287, 105, 133), roleMenu(288, 105, 134),
                roleMenu(289, 105, 1100), roleMenu(290, 105, 1110)
        );
    }

    private void assertMenuRowsExactly(String sql, List<MenuRow> expectedRows) {
        List<MenuRow> actualRows = menuRows(sql);
        for (MenuRow expected : expectedRows) {
            List<MenuRow> matchingIdRows = actualRows.stream()
                    .filter(row -> row.id() == expected.id())
                    .toList();
            assertEquals(List.of(expected), matchingIdRows,
                    "Expected exactly one full menu row for id=" + expected.id());
        }
    }

    private List<MenuRow> menuRowsForIds(String sql, List<Integer> ids) {
        return menuRows(sql).stream().filter(row -> ids.contains(row.id())).toList();
    }

    private List<MenuRow> menuRows(String sql) {
        Matcher matcher = MENU_ROW.matcher(sql);
        List<MenuRow> rows = new ArrayList<>();
        while (matcher.find()) {
            rows.add(menu(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    matcher.group(3),
                    Integer.parseInt(matcher.group(4)),
                    sqlValue(matcher.group(5)),
                    sqlValue(matcher.group(6)),
                    sqlValue(matcher.group(7)),
                    Integer.parseInt(matcher.group(8)),
                    Integer.parseInt(matcher.group(9))));
        }
        return rows;
    }

    private List<RoleMenuRow> seedRoleMenuRows(String sql) {
        Matcher matcher = SEED_ROLE_MENU_ROW.matcher(sql);
        List<RoleMenuRow> rows = new ArrayList<>();
        while (matcher.find()) {
            rows.add(roleMenu(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))));
        }
        return rows;
    }

    private List<RoleMenuRow> migrationRoleMenuRows(String sql) {
        Matcher matcher = MIGRATION_ROLE_MENU_ROW.matcher(sql);
        List<RoleMenuRow> rows = new ArrayList<>();
        while (matcher.find()) {
            int roleId = Integer.parseInt(matcher.group(2));
            int menuId = Integer.parseInt(matcher.group(3));
            assertEquals(roleId, Integer.parseInt(matcher.group(4)), "WHERE NOT EXISTS role must match SELECT");
            assertEquals(menuId, Integer.parseInt(matcher.group(5)), "WHERE NOT EXISTS menu must match SELECT");
            rows.add(roleMenu(Integer.parseInt(matcher.group(1)), roleId, menuId));
        }
        return rows;
    }

    private List<Integer> newMenuAssignments(List<RoleMenuRow> rows, int roleId) {
        return rows.stream()
                .filter(row -> row.roleId() == roleId && NEW_MENU_IDS.contains(row.menuId()))
                .map(RoleMenuRow::menuId)
                .toList();
    }

    private int countOccurrences(String value, String needle) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    private String sqlValue(String value) {
        return "NULL".equals(value) ? null : value.substring(1, value.length() - 1);
    }

    private MenuRow menu(int id, int parentId, String name, int type, String path,
                         String permission, String icon, int sortOrder, int status) {
        return new MenuRow(id, parentId, name, type, path, permission, icon, sortOrder, status);
    }

    private RoleMenuRow roleMenu(int id, int roleId, int menuId) {
        return new RoleMenuRow(id, roleId, menuId);
    }

    private String seedSql() throws IOException {
        return resourceSql("/sql/admin_system.sql");
    }

    private String migrationSql() throws IOException {
        return resourceSql("/sql/d00003_vin_logistics_migration.sql");
    }

    private String resourceSql(String path) throws IOException {
        InputStream stream = getClass().getResourceAsStream(path);
        assertNotNull(stream, "Missing SQL resource: " + path);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private record MenuRow(int id, int parentId, String name, int type, String path,
                           String permission, String icon, int sortOrder, int status) {
    }

    private record RoleMenuRow(int id, int roleId, int menuId) {
    }
}

package com.company.admin.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VinLogisticsSchemaSqlTest {

    @Test
    void fullAndMigrationSqlDefineVinLevelLogisticsTables() throws IOException {
        String full = resource("/sql/admin_system.sql");
        String migration = resource("/sql/d00003_vin_logistics_migration.sql");

        for (String sql : new String[]{full, migration}) {
            assertTrue(sql.contains("t_veh_inbound"));
            assertTrue(sql.contains("saic_buy_off_date"));
            assertTrue(sql.contains("date_to_storage_yard"));
            assertTrue(sql.contains("t_veh_delivery"));
            assertTrue(sql.contains("etd_to_dealer"));
            assertTrue(sql.contains("eta_to_dealer"));
            assertTrue(sql.contains("trolly_type"));
            assertTrue(sql.contains("fully_load"));
            assertTrue(sql.contains("received_date"));
            assertTrue(sql.contains("delivery_status"));
        }
    }

    @Test
    void deliveryTableCommentIsClosedInBothSqlScripts() throws IOException {
        for (String sql : new String[]{
                resource("/sql/admin_system.sql"),
                resource("/sql/d00003_vin_logistics_migration.sql")}) {
            assertTrue(sql.contains("COMMENT = '按 VIN 配送阶段';"));
        }
    }

    @Test
    void migrationDoesNotDropOrAlterLegacyLogisticsTables() throws IOException {
        assertDoesNotModifyLegacyLogisticsTables(resource("/sql/d00003_vin_logistics_migration.sql"));
    }

    @Test
    void vehicleTodoLookupHasLifecycleDeletedIdIndexInFullSchema() throws IOException {
        String vehicleTable = tableDefinition(resource("/sql/admin_system.sql"), "t_vehicle");
        assertTrue(Pattern.compile(
                "INDEX\\s+`idx_lifecycle_deleted_id`\\s*\\(\\s*`lifecycle_stage`(?:\\s+ASC)?\\s*,"
                        + "\\s*`deleted`(?:\\s+ASC)?\\s*,\\s*`id`(?:\\s+ASC)?\\s*\\)",
                Pattern.CASE_INSENSITIVE).matcher(vehicleTable).find(),
                "t_vehicle must index lifecycle_stage, deleted, id in query order");
    }

    @Test
    void migrationAddsVehicleTodoIndexOnlyWhenMissing() throws IOException {
        String migration = resource("/sql/d00003_vin_logistics_migration.sql");
        assertTrue(migration.contains("information_schema.statistics"));
        assertTrue(Pattern.compile("table_schema\\s*=\\s*DATABASE\\s*\\(\\s*\\)",
                Pattern.CASE_INSENSITIVE).matcher(migration).find());
        assertTrue(Pattern.compile("index_name\\s*=\\s*'idx_lifecycle_deleted_id'",
                Pattern.CASE_INSENSITIVE).matcher(migration).find());
        assertTrue(Pattern.compile(
                "ALTER\\s+TABLE\\s+`t_vehicle`\\s+ADD\\s+INDEX\\s+`idx_lifecycle_deleted_id`\\s*"
                        + "\\(\\s*`lifecycle_stage`\\s*,\\s*`deleted`\\s*,\\s*`id`\\s*\\)",
                Pattern.CASE_INSENSITIVE).matcher(migration).find());
        assertTrue(Pattern.compile(
                "SET\\s+@vlm_vehicle_todo_index_sql\\s*=\\s*IF\\s*\\(\\s*"
                        + "@vlm_vehicle_todo_index_exists\\s*=\\s*0\\s*,\\s*"
                        + "'ALTER TABLE `t_vehicle` ADD INDEX `idx_lifecycle_deleted_id` "
                        + "\\(`lifecycle_stage`, `deleted`, `id`\\)'\\s*,\\s*'SELECT 1'\\s*\\)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(migration).find(),
                "Index DDL must be selected only when the same existence check reports zero");
        assertTrue(Pattern.compile(
                "PREPARE\\s+vlm_vehicle_todo_index_stmt\\s+FROM\\s+@vlm_vehicle_todo_index_sql\\s*;\\s*"
                        + "EXECUTE\\s+vlm_vehicle_todo_index_stmt\\s*;\\s*"
                        + "DEALLOCATE\\s+PREPARE\\s+vlm_vehicle_todo_index_stmt\\s*;",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(migration).find(),
                "Migration must prepare, execute, and deallocate the selected index statement");
    }

    @Test
    void legacyProtectionDetectsQuotedAndIfExistsMySqlDdl() {
        assertThrows(AssertionError.class, () -> assertDoesNotModifyLegacyLogisticsTables(
                "DROP TABLE IF EXISTS `t_transport_order`;"));
        assertThrows(AssertionError.class, () -> assertDoesNotModifyLegacyLogisticsTables(
                "ALTER TABLE `t_transport_order` ADD COLUMN `bad` int;"));
    }

    private void assertDoesNotModifyLegacyLogisticsTables(String migration) {
        String[] legacyTables = {
                "T_TRANSPORT_ORDER", "T_TRANSPORT_ORDER_ITEM", "T_DISPATCH_LIST",
                "T_WAYBILL", "T_WAYBILL_DEALER", "T_WAYBILL_DEALER_VIN"
        };
        for (String table : legacyTables) {
            Pattern destructiveDdl = Pattern.compile(
                    "\\b(?:DROP|ALTER)\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?`?"
                            + Pattern.quote(table) + "`?(?![A-Z0-9_])",
                    Pattern.CASE_INSENSITIVE);
            assertFalse(destructiveDdl.matcher(migration).find(),
                    "Migration must not drop or alter legacy table " + table);
        }
    }

    private String tableDefinition(String sql, String table) {
        Matcher matcher = Pattern.compile(
                "CREATE\\s+TABLE\\s+`" + Pattern.quote(table) + "`\\s*\\((.*?)\\)\\s*ENGINE",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(sql);
        assertTrue(matcher.find(), "Missing table definition for " + table);
        return matcher.group(1);
    }

    private String resource(String path) throws IOException {
        var stream = getClass().getResourceAsStream(path);
        assertNotNull(stream, "Missing SQL resource: " + path);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}

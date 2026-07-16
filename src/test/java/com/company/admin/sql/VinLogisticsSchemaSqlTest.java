package com.company.admin.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void migrationDoesNotDropOrAlterLegacyLogisticsTables() throws IOException {
        String migration = resource("/sql/d00003_vin_logistics_migration.sql").toUpperCase();
        String[] legacyTables = {
                "T_TRANSPORT_ORDER", "T_TRANSPORT_ORDER_ITEM", "T_DISPATCH_LIST",
                "T_WAYBILL", "T_WAYBILL_DEALER", "T_WAYBILL_DEALER_VIN"
        };
        for (String table : legacyTables) {
            assertFalse(migration.contains("DROP TABLE " + table));
            assertFalse(migration.contains("ALTER TABLE " + table));
        }
    }

    private String resource(String path) throws IOException {
        var stream = getClass().getResourceAsStream(path);
        assertNotNull(stream, "Missing SQL resource: " + path);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}

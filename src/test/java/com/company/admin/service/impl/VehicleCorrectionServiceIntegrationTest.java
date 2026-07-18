package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.PaymentCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.ProductionCorrection;
import com.company.admin.service.VehicleCorrectionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/h2-mapper-smoke-schema.sql")
class VehicleCorrectionServiceIntegrationTest {

    private static final String CORRECTION_OPERATION = "车辆数据修订-字段差异";

    @Autowired
    private VehicleCorrectionService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void seedCompletedVehicleWithProductionAndPayment() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) "
                + "VALUES (90, 'VIN00000000000090', 'COMPLETED', 0)");
        jdbcTemplate.update("INSERT INTO t_md_model (id, model_name, status, deleted) "
                + "VALUES (911, 'Correction model', 1, 0)");
        jdbcTemplate.update("INSERT INTO t_md_exterior_color (id, color_name, status, deleted) "
                + "VALUES (912, 'Correction exterior', 1, 0)");
        jdbcTemplate.update("INSERT INTO t_md_interior_color (id, color_name, status, deleted) "
                + "VALUES (913, 'Correction interior', 1, 0)");
        jdbcTemplate.update("INSERT INTO t_veh_production "
                + "(id, vehicle_id, stage_status, model_id, exterior_color_id, interior_color_id, "
                + "engine_number, year_make, material, shipment, batch, offline_epmb_date, "
                + "epmb_ok_date, remark1, confirmed_by, deleted) "
                + "VALUES (901, 90, 'CONFIRMED', 911, 912, 913, 'OLD-ENGINE', '2026', "
                + "'OLD-MATERIAL', 'OLD-SHIPMENT', 'OLD-BATCH', '2026-01-01', '2026-01-02', "
                + "'old remark', 'original-confirmer', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_payment "
                + "(id, vehicle_id, stage_status, payment_date, payment_status, confirmed_by, deleted) "
                + "VALUES (902, 90, 'CONFIRMED', '2026-02-01', 'PAID', 'original-confirmer', 0)");
        jdbcTemplate.update("INSERT INTO t_sys_dict_type "
                + "(id, dict_code, dict_name, status, deleted) "
                + "VALUES (914, 'payment_status', 'Payment status', 1, 0)");
        jdbcTemplate.update("INSERT INTO t_sys_dict_item "
                + "(id, dict_type_id, item_value, item_label, sort_order, status, deleted) "
                + "VALUES (915, 914, 'PAID', 'Paid', 1, 1, 0)");
    }

    @Test
    void laterValidationFailureRollsBackEarlierProductionUpdate() {
        VehicleCorrectionUpdateRequest request = validProductionRequest("NEW-ENGINE");
        PaymentCorrection payment = new PaymentCorrection();
        payment.setId(902L);
        payment.setPaymentDate(LocalDate.of(2026, 2, 2));
        payment.setPaymentStatus("NOT_A_DICTIONARY_VALUE");
        request.setPayment(payment);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateCorrection(90L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), exception.getCode());
        assertEquals("OLD-ENGINE", jdbcTemplate.queryForObject(
                "SELECT engine_number FROM t_veh_production WHERE id=901", String.class));
        assertEquals("PAID", jdbcTemplate.queryForObject(
                "SELECT payment_status FROM t_veh_payment WHERE id=902", String.class));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_operation_log WHERE operation=?",
                Integer.class, CORRECTION_OPERATION));
    }

    @Test
    @WithMockUser(username = "correction-admin")
    void successfulCorrectionPersistsStructuredBeforeAndAfterAudit() throws Exception {
        service.updateCorrection(90L, validProductionRequest("NEW-ENGINE"));

        assertEquals("NEW-ENGINE", jdbcTemplate.queryForObject(
                "SELECT engine_number FROM t_veh_production WHERE id=901", String.class));
        assertEquals("CONFIRMED", jdbcTemplate.queryForObject(
                "SELECT stage_status FROM t_veh_production WHERE id=901", String.class));
        assertEquals("original-confirmer", jdbcTemplate.queryForObject(
                "SELECT confirmed_by FROM t_veh_production WHERE id=901", String.class));

        String params = jdbcTemplate.queryForObject(
                "SELECT params FROM sys_operation_log WHERE operation=?",
                String.class, CORRECTION_OPERATION);
        JsonNode audit = objectMapper.readTree(params);
        assertEquals(90L, audit.path("vehicleId").asLong());
        assertEquals("VIN00000000000090", audit.path("vin").asText());
        assertEquals("OLD-ENGINE",
                audit.path("before").path("production:901").path("engineNumber").asText());
        assertEquals("NEW-ENGINE",
                audit.path("after").path("production:901").path("engineNumber").asText());
        assertTrue(audit.path("before").path("production:901").has("modelId"));
        assertFalse(audit.path("before").path("production:901").has("stageStatus"));
        assertFalse(audit.path("after").path("production:901").has("confirmedBy"));
        assertEquals("correction-admin", jdbcTemplate.queryForObject(
                "SELECT username FROM sys_operation_log WHERE operation=?",
                String.class, CORRECTION_OPERATION));
        assertEquals("SUCCESS", jdbcTemplate.queryForObject(
                "SELECT result FROM sys_operation_log WHERE operation=?",
                String.class, CORRECTION_OPERATION));
    }

    @Test
    @WithMockUser(username = "correction-admin")
    void correctionCanClearOptionalDateAndRemarkInDatabaseAndAudit() throws Exception {
        VehicleCorrectionUpdateRequest request = validProductionRequest("NEW-ENGINE");
        request.getProduction().setOfflineEpmbDate(null);
        request.getProduction().setRemark1(null);

        service.updateCorrection(90L, request);

        assertNull(jdbcTemplate.queryForObject(
                "SELECT offline_epmb_date FROM t_veh_production WHERE id=901", LocalDate.class));
        assertNull(jdbcTemplate.queryForObject(
                "SELECT remark1 FROM t_veh_production WHERE id=901", String.class));

        String params = jdbcTemplate.queryForObject(
                "SELECT params FROM sys_operation_log WHERE operation=?",
                String.class, CORRECTION_OPERATION);
        JsonNode after = objectMapper.readTree(params)
                .path("after").path("production:901");
        assertTrue(after.has("offlineEpmbDate"));
        assertTrue(after.path("offlineEpmbDate").isNull());
        assertTrue(after.has("remark1"));
        assertTrue(after.path("remark1").isNull());
    }

    private VehicleCorrectionUpdateRequest validProductionRequest(String engineNumber) {
        ProductionCorrection production = new ProductionCorrection();
        production.setId(901L);
        production.setModelId(911L);
        production.setExteriorColorId(912L);
        production.setInteriorColorId(913L);
        production.setEngineNumber(engineNumber);
        production.setYearMake("2027");
        production.setMaterial("NEW-MATERIAL");
        production.setShipment("NEW-SHIPMENT");
        production.setBatch("NEW-BATCH");
        production.setOfflineEpmbDate(LocalDate.of(2026, 3, 1));
        production.setEpmbOkDate(LocalDate.of(2026, 3, 2));
        production.setRemark1("corrected remark");

        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setProduction(production);
        return request;
    }
}

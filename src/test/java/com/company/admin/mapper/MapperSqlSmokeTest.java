package com.company.admin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.InvoiceQueryRequest;
import com.company.admin.dto.request.PaymentQueryRequest;
import com.company.admin.dto.response.InvoiceListResponse;
import com.company.admin.dto.response.PaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/h2-mapper-smoke-schema.sql")
class MapperSqlSmokeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private VehInvoiceMapper vehInvoiceMapper;
    @Autowired
    private VehPaymentMapper vehPaymentMapper;
    @Autowired
    private UserMapper userMapper;

    @Test
    void invoicePageIncludesPendingInvoiceVehiclesWithoutInvoiceRows() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (1, 'VIN00000000000001', 'PENDING_INVOICE', 0)");

        Page<InvoiceListResponse> page = vehInvoiceMapper.selectInvoicePage(
                new Page<>(1, 10), new InvoiceQueryRequest());

        assertEquals(1, page.getRecords().size());
        InvoiceListResponse row = page.getRecords().get(0);
        assertEquals(1L, row.getVehicleId());
        assertEquals("VIN00000000000001", row.getVin());
        assertFalse(row.getHasFormalInvoice());
        assertEquals(0, row.getInvoiceCount());
    }

    @Test
    void paymentPageRequiresConfirmedFormalInvoiceForHasFormalInvoice() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (2, 'VIN00000000000002', 'PENDING_PAYMENT', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_invoice (vehicle_id, stage_status, invoice_seq, invoice_type, invoice_no, deleted) VALUES (2, 'CONFIRMED', 1, 'PROFORMA_INVOICED', 'PF-001', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_invoice (vehicle_id, stage_status, invoice_seq, invoice_type, invoice_no, deleted) VALUES (2, 'DRAFT', 2, 'INVOICED', 'INV-001', 0)");

        Page<PaymentResponse> page = vehPaymentMapper.selectPaymentPage(
                new Page<>(1, 10), new PaymentQueryRequest());

        assertEquals(1, page.getRecords().size());
        PaymentResponse row = page.getRecords().get(0);
        assertEquals(2L, row.getVehicleId());
        assertFalse(row.getHasFormalInvoice());
    }

    @Test
    void userMapperReturnsRoleCodes() {
        jdbcTemplate.update("INSERT INTO sys_role (id, role_code, status, deleted) VALUES (10, 'ADMIN', 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 10)");

        List<String> roleCodes = userMapper.selectRoleCodesByUserId(1L);

        assertEquals(List.of("ADMIN"), roleCodes);
    }

    @Test
    void paymentPageCanFilterVehiclesWithConfirmedFormalInvoice() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (3, 'VIN00000000000003', 'PENDING_PAYMENT', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_invoice (vehicle_id, stage_status, invoice_seq, invoice_type, invoice_no, deleted) VALUES (3, 'CONFIRMED', 1, 'INVOICED', 'INV-003', 0)");
        PaymentQueryRequest request = new PaymentQueryRequest();
        request.setHasFormalInvoice(true);

        Page<PaymentResponse> page = vehPaymentMapper.selectPaymentPage(new Page<>(1, 10), request);

        assertEquals(1, page.getRecords().size());
        assertTrue(page.getRecords().get(0).getHasFormalInvoice());
    }
}

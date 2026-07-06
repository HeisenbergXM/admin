package com.company.admin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.AllocationQueryRequest;
import com.company.admin.dto.request.InvoiceQueryRequest;
import com.company.admin.dto.request.PaymentQueryRequest;
import com.company.admin.dto.response.AllocationResponse;
import com.company.admin.dto.response.InvoiceListResponse;
import com.company.admin.dto.response.PaymentResponse;
import com.company.admin.entity.Menu;
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
import static org.springframework.beans.PropertyAccessorFactory.forBeanPropertyAccess;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/h2-mapper-smoke-schema.sql")
class MapperSqlSmokeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private VehAllocationMapper vehAllocationMapper;
    @Autowired
    private VehInvoiceMapper vehInvoiceMapper;
    @Autowired
    private VehPaymentMapper vehPaymentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MenuMapper menuMapper;

    @Test
    void allocationPageIncludesPendingAllocationVehiclesWithoutAllocationRows() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (4, 'VIN00000000000004', 'PENDING_ALLOCATION', 0)");
        jdbcTemplate.update("INSERT INTO t_md_model (id, model_name, series, spec, model_code, deleted) VALUES (41, 'MG4 EV', 'MG', 'Luxury', 'MG4-LUX', 0)");
        jdbcTemplate.update("INSERT INTO t_md_exterior_color (id, color_name, deleted) VALUES (42, 'Moon White', 0)");
        jdbcTemplate.update("INSERT INTO t_md_interior_color (id, color_name, deleted) VALUES (43, 'Cloud Gray', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_production (vehicle_id, stage_status, model_id, year_make, exterior_color_id, interior_color_id, deleted) VALUES (4, 'CONFIRMED', 41, '2026', 42, 43, 0)");
        AllocationQueryRequest request = new AllocationQueryRequest();
        request.setStageStatus("PENDING_ALLOCATION");

        Page<AllocationResponse> page = vehAllocationMapper.selectAllocationPage(
                new Page<>(1, 10), request);

        assertEquals(1, page.getRecords().size());
        AllocationResponse row = page.getRecords().get(0);
        assertEquals(4L, row.getVehicleId());
        assertEquals("VIN00000000000004", row.getVin());
        assertEquals("PENDING_ALLOCATION", row.getStageStatus());
        assertEquals("PENDING_ALLOCATION", forBeanPropertyAccess(row).getPropertyValue("lifecycleStage"));
        assertEquals("MG4 EV", forBeanPropertyAccess(row).getPropertyValue("modelName"));
        assertEquals("Moon White", forBeanPropertyAccess(row).getPropertyValue("exteriorColorName"));
        assertEquals("2026", forBeanPropertyAccess(row).getPropertyValue("yearMake"));
    }

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
    void adminUserGetsAllEnabledPermissionsWithoutRoleMenuRows() {
        jdbcTemplate.update("INSERT INTO sys_role (id, role_code, status, deleted) VALUES (12, 'ADMIN', 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (3, 12)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (200, 0, '用户管理', 2, '/system/user', 'sys:user:list', 1, 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (201, 0, '新增用户', 3, NULL, 'sys:user:add', 2, 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (202, 0, '停用菜单', 2, '/disabled', 'sys:disabled:list', 3, 0, 0)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (203, 0, '目录', 1, '/dir', NULL, 4, 1, 0)");

        List<String> permissions = userMapper.selectPermissionsByUserId(3L);

        assertEquals(List.of("sys:user:list", "sys:user:add"), permissions);
    }

    @Test
    void adminUserGetsAllEnabledMenusWithoutRoleMenuRows() {
        jdbcTemplate.update("INSERT INTO sys_role (id, role_code, status, deleted) VALUES (13, 'ADMIN', 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (4, 13)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (210, 0, '系统管理', 1, '/system', NULL, 1, 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (211, 210, '用户管理', 2, '/system/user', 'sys:user:list', 2, 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (212, 211, '新增用户', 3, NULL, 'sys:user:add', 3, 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (213, 0, '停用菜单', 2, '/disabled', 'sys:disabled:list', 4, 0, 0)");

        List<Menu> menus = menuMapper.selectByUserId(4L);

        assertEquals(List.of(210L, 211L, 212L), menus.stream().map(Menu::getId).toList());
    }

    @Test
    void userMenusIncludeAncestorsWhenOnlyLeafPermissionIsAssigned() {
        jdbcTemplate.update("INSERT INTO sys_role (id, role_code, status, deleted) VALUES (11, 'MENU_TEST', 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 11)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (100, 0, '系统管理', 1, '/system', NULL, 1, 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (101, 100, '菜单管理', 2, '/system/menu', 'sys:menu:list', 2, 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, sort_order, status, deleted) VALUES (102, 101, '新增菜单', 3, NULL, 'sys:menu:add', 3, 1, 0)");
        jdbcTemplate.update("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (11, 102)");

        List<Menu> menus = menuMapper.selectByUserId(2L);

        assertEquals(List.of(100L, 101L, 102L), menus.stream().map(Menu::getId).toList());
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

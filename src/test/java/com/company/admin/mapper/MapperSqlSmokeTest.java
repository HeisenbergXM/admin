package com.company.admin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.AllocationQueryRequest;
import com.company.admin.dto.request.DeliveryQueryRequest;
import com.company.admin.dto.request.InboundQueryRequest;
import com.company.admin.dto.request.InvoiceQueryRequest;
import com.company.admin.dto.request.PaymentQueryRequest;
import com.company.admin.dto.request.RegistrationQueryRequest;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.request.DeliverySaveRequest;
import com.company.admin.dto.request.InboundSaveRequest;
import com.company.admin.dto.response.AllocationResponse;
import com.company.admin.dto.response.DeliveryResponse;
import com.company.admin.dto.response.InboundResponse;
import com.company.admin.dto.response.InvoiceListResponse;
import com.company.admin.dto.response.PaymentResponse;
import com.company.admin.dto.response.RegistrationResponse;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.entity.Menu;
import com.company.admin.entity.VehDelivery;
import com.company.admin.entity.VehInbound;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.service.VehDeliveryService;
import com.company.admin.service.VehInboundService;
import com.company.admin.service.VehicleCorrectionAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.beans.PropertyAccessorFactory.forBeanPropertyAccess;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/h2-mapper-smoke-schema.sql")
class MapperSqlSmokeTest {

    @MockBean
    private VehicleCorrectionAuditService vehicleCorrectionAuditService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private VehDeliveryService vehDeliveryService;
    @Autowired
    private VehInboundService vehInboundService;
    @Autowired
    private VehAllocationMapper vehAllocationMapper;
    @Autowired
    private VehDeliveryMapper vehDeliveryMapper;
    @Autowired
    private VehInboundMapper vehInboundMapper;
    @Autowired
    private VehInvoiceMapper vehInvoiceMapper;
    @Autowired
    private VehPaymentMapper vehPaymentMapper;
    @Autowired
    private VehRegistrationMapper vehRegistrationMapper;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MenuMapper menuMapper;

    @Test
    void inboundUpdateWaitsForLockThenRejectsConfirmedRowWithoutOverwriting() throws Exception {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (28, 'VIN00000000000028', 'PENDING_INBOUND', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_inbound (id, vehicle_id, stage_status, saic_buy_off_date, date_to_storage_yard, deleted) VALUES (280, 28, 'DRAFT', '2026-07-14', '2026-07-15', 0)");
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        CountDownLatch firstHasLock = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> transaction.executeWithoutResult(status -> {
                VehInbound locked = vehInboundMapper.selectByVehicleIdForUpdate(28L);
                assertEquals("DRAFT", locked.getStageStatus());
                jdbcTemplate.update("UPDATE t_veh_inbound SET stage_status = 'CONFIRMED' WHERE vehicle_id = 28");
                firstHasLock.countDown();
                awaitRelease(releaseFirst);
            }));
            assertTrue(firstHasLock.await(5, TimeUnit.SECONDS));
            InboundSaveRequest staleUpdate = new InboundSaveRequest();
            staleUpdate.setSaicBuyOffDate(LocalDate.of(2026, 8, 1));
            staleUpdate.setDateToStorageYard(LocalDate.of(2026, 8, 2));
            Future<BusinessException> second = executor.submit(() -> {
                secondStarted.countDown();
                return assertThrows(BusinessException.class,
                        () -> vehInboundService.updateInbound(28L, staleUpdate));
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));

            assertThrows(TimeoutException.class, () -> second.get(200, TimeUnit.MILLISECONDS));
            releaseFirst.countDown();
            first.get(5, TimeUnit.SECONDS);
            assertEquals(ErrorCode.STAGE_ALREADY_CONFIRMED.getCode(),
                    second.get(5, TimeUnit.SECONDS).getCode());
            assertEquals("CONFIRMED", jdbcTemplate.queryForObject(
                    "SELECT stage_status FROM t_veh_inbound WHERE vehicle_id = 28", String.class));
            assertEquals(LocalDate.of(2026, 7, 14), jdbcTemplate.queryForObject(
                    "SELECT saic_buy_off_date FROM t_veh_inbound WHERE vehicle_id = 28", LocalDate.class));
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void deliveryConfirmWaitsForLockThenReturnsAlreadyConfirmed() throws Exception {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (48, 'VIN00000000000048', 'PENDING_DELIVERY', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_delivery (id, vehicle_id, stage_status, deleted) VALUES (480, 48, 'DRAFT', 0)");
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        CountDownLatch firstHasLock = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> transaction.executeWithoutResult(status -> {
                VehDelivery locked = vehDeliveryMapper.selectByVehicleIdForUpdate(48L);
                assertEquals("DRAFT", locked.getStageStatus());
                jdbcTemplate.update("UPDATE t_veh_delivery SET stage_status = 'CONFIRMED', confirmed_by = 'first-request' WHERE vehicle_id = 48");
                firstHasLock.countDown();
                awaitRelease(releaseFirst);
            }));
            assertTrue(firstHasLock.await(5, TimeUnit.SECONDS));

            Future<BusinessException> second = executor.submit(() -> {
                secondStarted.countDown();
                return assertThrows(BusinessException.class,
                        () -> vehDeliveryService.confirmDelivery(48L));
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));

            assertThrows(TimeoutException.class, () -> second.get(200, TimeUnit.MILLISECONDS));
            releaseFirst.countDown();
            first.get(5, TimeUnit.SECONDS);
            assertEquals(ErrorCode.STAGE_ALREADY_CONFIRMED.getCode(),
                    second.get(5, TimeUnit.SECONDS).getCode());
            assertEquals("CONFIRMED", jdbcTemplate.queryForObject(
                    "SELECT stage_status FROM t_veh_delivery WHERE vehicle_id = 48", String.class));
            assertEquals("first-request", jdbcTemplate.queryForObject(
                    "SELECT confirmed_by FROM t_veh_delivery WHERE vehicle_id = 48", String.class));
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    private void awaitRelease(CountDownLatch releaseFirst) {
        try {
            assertTrue(releaseFirst.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Test
    void deliveryDefaultPageIncludesPendingVehicleWithoutDraft() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (40, 'VIN00000000000040', 'PENDING_DELIVERY', 0)");

        Page<DeliveryResponse> page = vehDeliveryMapper.selectDeliveryPage(
                new Page<>(1, 10), new DeliveryQueryRequest());

        assertEquals(1, page.getRecords().size());
        DeliveryResponse row = page.getRecords().get(0);
        assertNull(row.getId());
        assertEquals(40L, row.getVehicleId());
        assertEquals("PENDING_DELIVERY", row.getStageStatus());
    }

    @Test
    void deliveryByVehicleIdExposesSavedDraftFields() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (41, 'VIN00000000000041', 'PENDING_DELIVERY', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_delivery (id, vehicle_id, stage_status, etd_to_dealer, eta_to_dealer, trolly_type, fully_load, received_date, delivery_status, remark7, deleted) VALUES (410, 41, 'DRAFT', '2026-07-17', '2026-07-18', 'OPEN', 1, '2026-07-19', 'DELIVERED', 'draft delivery', 0)");

        DeliveryResponse row = vehDeliveryMapper.selectDeliveryByVehicleId(41L);

        assertEquals(410L, row.getId());
        assertEquals("DRAFT", row.getStageStatus());
        assertEquals(LocalDate.of(2026, 7, 17), row.getEtdToDealer());
        assertEquals(LocalDate.of(2026, 7, 18), row.getEtaToDealer());
        assertEquals("OPEN", row.getTrollyType());
        assertTrue(row.getFullyLoad());
        assertEquals(LocalDate.of(2026, 7, 19), row.getReceivedDate());
        assertEquals("DELIVERED", row.getDeliveryStatus());
        assertEquals("draft delivery", row.getRemark7());
    }

    @Test
    void deliveryPageReadsDealerFromConfirmedAllocation() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (42, 'VIN00000000000042', 'PENDING_DELIVERY', 0)");
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (43, 'VIN00000000000043', 'PENDING_DELIVERY', 0)");
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (44, 'VIN00000000000044', 'PENDING_DELIVERY', 0)");
        jdbcTemplate.update("INSERT INTO t_md_dealer (id, dealer_code, dealer_name, deleted) VALUES (400, 'D400', 'Phoenix Dealer', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_allocation (vehicle_id, stage_status, dealer_id, deleted) VALUES (42, 'CONFIRMED', 400, 0)");
        jdbcTemplate.update("INSERT INTO t_veh_allocation (vehicle_id, stage_status, dealer_id, deleted) VALUES (43, 'DRAFT', 400, 0)");
        jdbcTemplate.update("INSERT INTO t_veh_allocation (vehicle_id, stage_status, dealer_id, deleted) VALUES (44, 'CONFIRMED', 400, 1)");
        DeliveryQueryRequest request = new DeliveryQueryRequest();
        request.setDealerId(400L);

        Page<DeliveryResponse> page = vehDeliveryMapper.selectDeliveryPage(new Page<>(1, 10), request);

        assertEquals(List.of(42L), page.getRecords().stream().map(DeliveryResponse::getVehicleId).toList());
        assertEquals(400L, page.getRecords().get(0).getDealerId());
        assertEquals("D400", page.getRecords().get(0).getDealerCode());
        assertEquals("Phoenix Dealer", page.getRecords().get(0).getDealerName());
    }

    @Test
    void deliveryConfirmedFilterExcludesAdvancedVehicle() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (65, 'VIN00000000000065', 'PENDING_REGISTRATION', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_delivery (vehicle_id, stage_status, delivery_status, deleted) VALUES (65, 'CONFIRMED', 'DELIVERED', 0)");
        DeliveryQueryRequest request = new DeliveryQueryRequest();
        request.setStageStatus("CONFIRMED");

        Page<DeliveryResponse> page = vehDeliveryMapper.selectDeliveryPage(new Page<>(1, 10), request);

        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void inboundDefaultPageIncludesPendingVehicleWithoutDraft() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (20, 'VIN00000000000020', 'PENDING_INBOUND', 0)");
        InboundQueryRequest request = new InboundQueryRequest();

        Page<InboundResponse> page = vehInboundMapper.selectInboundPage(new Page<>(1, 10), request);

        assertEquals(1, page.getRecords().size());
        InboundResponse row = page.getRecords().get(0);
        assertNull(row.getId());
        assertEquals("PENDING_INBOUND", row.getStageStatus());
    }

    @Test
    void inboundPendingPageExposesSavedDraftAndVehicleMasterData() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (21, 'VIN00000000000021', 'PENDING_INBOUND', 0)");
        jdbcTemplate.update("INSERT INTO t_md_model (id, model_name, deleted) VALUES (210, 'MG4 EV', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_production (vehicle_id, stage_status, model_id, year_make, deleted) VALUES (21, 'CONFIRMED', 210, '2026', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_inbound (id, vehicle_id, stage_status, date_to_storage_yard, deleted) VALUES (211, 21, 'DRAFT', '2026-07-15', 0)");

        Page<InboundResponse> page = vehInboundMapper.selectInboundPage(new Page<>(1, 10), new InboundQueryRequest());

        InboundResponse row = page.getRecords().get(0);
        assertEquals("DRAFT", row.getStageStatus());
        assertEquals("MG4 EV", row.getModelName());
        assertEquals("2026", row.getYearMake());
    }

    @Test
    void inboundConfirmedFilterExcludesAdvancedVehicleByStorageDate() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (61, 'VIN00000000000061', 'PENDING_ALLOCATION', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_inbound (vehicle_id, stage_status, date_to_storage_yard, deleted) VALUES (61, 'CONFIRMED', '2026-07-14', 0)");
        InboundQueryRequest request = new InboundQueryRequest();
        request.setStageStatus("CONFIRMED");
        request.setStorageStartDate(LocalDate.of(2026, 7, 14));
        request.setStorageEndDate(LocalDate.of(2026, 7, 14));

        Page<InboundResponse> page = vehInboundMapper.selectInboundPage(new Page<>(1, 10), request);

        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void vehiclePageExcludesAdvancedVehicle() {
        jdbcTemplate.execute("ALTER TABLE t_vehicle ADD create_time TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE t_vehicle ADD update_time TIMESTAMP");
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (60, 'VIN00000000000060', 'PENDING_INBOUND', 0)");

        Page<VehicleListResponse> page = vehicleMapper.selectVehiclePage(
                new Page<>(1, 10), new VehicleQueryRequest());

        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void allocationConfirmedFilterExcludesAdvancedVehicle() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (62, 'VIN00000000000062', 'PENDING_INVOICE', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_allocation (vehicle_id, stage_status, deleted) VALUES (62, 'CONFIRMED', 0)");
        AllocationQueryRequest request = new AllocationQueryRequest();
        request.setStageStatus("CONFIRMED");

        Page<AllocationResponse> page = vehAllocationMapper.selectAllocationPage(new Page<>(1, 10), request);

        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void invoicePageExcludesAdvancedVehicle() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (63, 'VIN00000000000063', 'PENDING_PAYMENT', 0)");

        Page<InvoiceListResponse> page = vehInvoiceMapper.selectInvoicePage(
                new Page<>(1, 10), new InvoiceQueryRequest());

        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void paymentConfirmedFilterExcludesAdvancedVehicle() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (64, 'VIN00000000000064', 'PENDING_DELIVERY', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_payment (vehicle_id, stage_status, deleted) VALUES (64, 'CONFIRMED', 0)");
        PaymentQueryRequest request = new PaymentQueryRequest();
        request.setStageStatus("CONFIRMED");

        Page<PaymentResponse> page = vehPaymentMapper.selectPaymentPage(new Page<>(1, 10), request);

        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void registrationConfirmedFilterExcludesAdvancedVehicle() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (66, 'VIN00000000000066', 'COMPLETED', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_registration (vehicle_id, stage_status, deleted) VALUES (66, 'CONFIRMED', 0)");
        RegistrationQueryRequest request = new RegistrationQueryRequest();
        request.setStageStatus("CONFIRMED");

        Page<RegistrationResponse> page = vehRegistrationMapper.selectRegistrationPage(new Page<>(1, 10), request);

        assertTrue(page.getRecords().isEmpty());
    }

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
    void invoicePageExposesLatestDraftStatusForSavedInvoiceDrafts() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (5, 'VIN00000000000005', 'PENDING_INVOICE', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_invoice (id, vehicle_id, stage_status, invoice_seq, invoice_type, invoice_no, deleted) VALUES (50, 5, 'DRAFT', 1, 'INVOICED', 'INV-DRAFT', 0)");

        Page<InvoiceListResponse> page = vehInvoiceMapper.selectInvoicePage(
                new Page<>(1, 10), new InvoiceQueryRequest());

        assertEquals(1, page.getRecords().size());
        InvoiceListResponse row = page.getRecords().get(0);
        assertEquals(50L, row.getLatestInvoiceId());
        assertEquals("DRAFT", forBeanPropertyAccess(row).getPropertyValue("latestStageStatus"));
        assertEquals("PENDING_INVOICE", forBeanPropertyAccess(row).getPropertyValue("lifecycleStage"));
    }

    @Test
    void allocationPageExposesDraftStatusForSavedAllocationDrafts() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (6, 'VIN00000000000006', 'PENDING_ALLOCATION', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_allocation (id, vehicle_id, stage_status, dealer_id, deleted) VALUES (60, 6, 'DRAFT', 600, 0)");
        AllocationQueryRequest request = new AllocationQueryRequest();
        request.setStageStatus("PENDING_ALLOCATION");

        Page<AllocationResponse> page = vehAllocationMapper.selectAllocationPage(
                new Page<>(1, 10), request);

        assertEquals(1, page.getRecords().size());
        AllocationResponse row = page.getRecords().get(0);
        assertEquals(60L, row.getId());
        assertEquals("DRAFT", row.getStageStatus());
        assertEquals("PENDING_ALLOCATION", row.getLifecycleStage());
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
    void paymentPageFiltersPendingPaymentByVehicleLifecycleWhenPaymentRecordIsDraft() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (7, 'VIN00000000000007', 'PENDING_PAYMENT', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_payment (id, vehicle_id, stage_status, payment_status, deleted) VALUES (70, 7, 'DRAFT', 'PENDING', 0)");
        PaymentQueryRequest request = new PaymentQueryRequest();
        request.setStageStatus("PENDING_PAYMENT");

        Page<PaymentResponse> page = vehPaymentMapper.selectPaymentPage(
                new Page<>(1, 10), request);

        assertEquals(1, page.getRecords().size());
        PaymentResponse row = page.getRecords().get(0);
        assertEquals(70L, row.getId());
        assertEquals("DRAFT", row.getStageStatus());
        assertEquals("PENDING_PAYMENT", forBeanPropertyAccess(row).getPropertyValue("lifecycleStage"));
    }

    @Test
    void registrationPageFiltersPendingRegistrationByVehicleLifecycleWhenRegistrationRecordIsDraft() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (8, 'VIN00000000000008', 'PENDING_REGISTRATION', 0)");
        jdbcTemplate.update("INSERT INTO t_veh_registration (id, vehicle_id, stage_status, drosstech_status, deleted) VALUES (80, 8, 'DRAFT', 'UPLOADED', 0)");
        RegistrationQueryRequest request = new RegistrationQueryRequest();
        request.setStageStatus("PENDING_REGISTRATION");

        Page<RegistrationResponse> page = vehRegistrationMapper.selectRegistrationPage(
                new Page<>(1, 10), request);

        assertEquals(1, page.getRecords().size());
        RegistrationResponse row = page.getRecords().get(0);
        assertEquals(80L, row.getId());
        assertEquals("DRAFT", row.getStageStatus());
        assertEquals("PENDING_REGISTRATION", forBeanPropertyAccess(row).getPropertyValue("lifecycleStage"));
    }

    @Test
    void registrationPageIncludesPendingRegistrationVehiclesWithoutRegistrationRows() {
        jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (9, 'VIN00000000000009', 'PENDING_REGISTRATION', 0)");
        RegistrationQueryRequest request = new RegistrationQueryRequest();
        request.setStageStatus("PENDING_REGISTRATION");

        Page<RegistrationResponse> page = vehRegistrationMapper.selectRegistrationPage(
                new Page<>(1, 10), request);

        assertEquals(1, page.getRecords().size());
        RegistrationResponse row = page.getRecords().get(0);
        assertEquals(9L, row.getVehicleId());
        assertEquals("PENDING_REGISTRATION", row.getStageStatus());
        assertEquals("PENDING_REGISTRATION", forBeanPropertyAccess(row).getPropertyValue("lifecycleStage"));
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

    @Test
    void adminSystemSeedDefinesInvoiceConfirmPermission() throws IOException {
        String sql = new String(
                getClass().getResourceAsStream("/sql/admin_system.sql").readAllBytes(),
                StandardCharsets.UTF_8);

        assertTrue(sql.contains("'vlm:invoice:confirm'"));
    }

    @Test
    void adminSystemSeedUsesChineseBusinessDictionaryLabels() throws IOException {
        String sql = new String(
                getClass().getResourceAsStream("/sql/admin_system.sql").readAllBytes(),
                StandardCharsets.UTF_8);

        assertTrue(sql.contains("'invoice_status', '发票类型/状态'"));
        assertTrue(sql.contains("'INVOICED', '正式发票'"));
        assertTrue(sql.contains("'PROFORMA_INVOICED', '形式发票'"));
        assertTrue(sql.contains("'UNPAID', '未收款'"));
        assertTrue(sql.contains("'IN_TRANSIT', '运输中'"));
        assertTrue(sql.contains("'PENDING', '待上传'"));
        assertTrue(sql.contains("'ALLOCATED', '已分配'"));
    }
}

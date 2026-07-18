package com.company.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.AllocationCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.DeliveryCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.InboundCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.InvoiceCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.PaymentCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.ProductionCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.RegistrationCorrection;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.entity.VehAllocation;
import com.company.admin.entity.VehDelivery;
import com.company.admin.entity.VehInbound;
import com.company.admin.entity.VehInvoice;
import com.company.admin.entity.VehPayment;
import com.company.admin.entity.VehProduction;
import com.company.admin.entity.VehRegistration;
import com.company.admin.entity.Vehicle;
import com.company.admin.mapper.DealerMapper;
import com.company.admin.mapper.ExteriorColorMapper;
import com.company.admin.mapper.InteriorColorMapper;
import com.company.admin.mapper.VehAllocationMapper;
import com.company.admin.mapper.VehDeliveryMapper;
import com.company.admin.mapper.VehInboundMapper;
import com.company.admin.mapper.VehInvoiceMapper;
import com.company.admin.mapper.VehPaymentMapper;
import com.company.admin.mapper.VehProductionMapper;
import com.company.admin.mapper.VehRegistrationMapper;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.mapper.VehicleModelMapper;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.VehicleCorrectionAuditService;
import com.company.admin.service.VehicleCorrectionAuditService.CorrectionDiff;
import com.company.admin.service.VehiclePanoramaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleCorrectionServiceImplTest {

    @Mock
    private VehicleMapper vehicleMapper;
    @Mock
    private VehiclePanoramaService vehiclePanoramaService;
    @Mock
    private BusinessStatusLabelService statusLabelService;
    @Mock
    private VehProductionMapper vehProductionMapper;
    @Mock
    private VehInboundMapper vehInboundMapper;
    @Mock
    private VehAllocationMapper vehAllocationMapper;
    @Mock
    private VehInvoiceMapper vehInvoiceMapper;
    @Mock
    private VehPaymentMapper vehPaymentMapper;
    @Mock
    private VehDeliveryMapper vehDeliveryMapper;
    @Mock
    private VehRegistrationMapper vehRegistrationMapper;
    @Mock
    private VehicleModelMapper vehicleModelMapper;
    @Mock
    private ExteriorColorMapper exteriorColorMapper;
    @Mock
    private InteriorColorMapper interiorColorMapper;
    @Mock
    private DealerMapper dealerMapper;
    @Mock
    private VehicleCorrectionAuditService auditService;

    private VehicleCorrectionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VehicleCorrectionServiceImpl(
                vehicleMapper, vehiclePanoramaService, statusLabelService,
                vehProductionMapper, vehInboundMapper, vehAllocationMapper,
                vehInvoiceMapper, vehPaymentMapper, vehDeliveryMapper,
                vehRegistrationMapper, vehicleModelMapper, exteriorColorMapper,
                interiorColorMapper, dealerMapper, auditService);
    }

    @Test
    void pageCorrectionsUsesUnscopedCorrectionQueryAndAddsLabels() {
        VehicleQueryRequest request = new VehicleQueryRequest();
        Page<VehicleListResponse> page = new Page<>(1, 10);
        VehicleListResponse completed = new VehicleListResponse();
        completed.setLifecycleStage("COMPLETED");
        completed.setProductionStatus("CONFIRMED");
        page.setRecords(List.of(completed));
        page.setTotal(1);
        when(vehicleMapper.selectVehicleCorrectionPage(any(), eq(request))).thenReturn(page);
        when(statusLabelService.lifecycleStageLabel("COMPLETED")).thenReturn("Completed");
        when(statusLabelService.stageStatusLabel("CONFIRMED")).thenReturn("Confirmed");

        PageResult<VehicleListResponse> result = service.pageCorrections(request);

        assertEquals(1, result.getTotal());
        assertEquals("COMPLETED", result.getList().get(0).getLifecycleStage());
        assertEquals("Completed", result.getList().get(0).getLifecycleStageLabel());
        assertEquals("Confirmed", result.getList().get(0).getProductionStatusLabel());
        verify(vehicleMapper, never()).selectVehiclePage(any(), any());
    }

    @Test
    void getCorrectionResolvesVehicleByIdAndReturnsPanorama() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(70L);
        vehicle.setVin("VIN00000000000070");
        VehiclePanoramaResponse panorama = new VehiclePanoramaResponse();
        when(vehicleMapper.selectById(70L)).thenReturn(vehicle);
        when(vehiclePanoramaService.getPanorama(vehicle.getVin())).thenReturn(panorama);

        assertSame(panorama, service.getCorrection(70L));
        verify(vehiclePanoramaService).getPanorama(vehicle.getVin());
    }

    @Test
    void getCorrectionRejectsDeletedOrMissingVehicle() {
        when(vehicleMapper.selectById(70L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.getCorrection(70L));

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void getCorrectionRejectsDeletedVehicle() {
        Vehicle deletedVehicle = new Vehicle();
        deletedVehicle.setDeleted(1);
        when(vehicleMapper.selectById(70L)).thenReturn(deletedVehicle);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.getCorrection(70L));

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void updateConfirmedProductionChangesOnlyBusinessFields() {
        Vehicle vehicle = vehicle(80L, "VIN00000000000080", "COMPLETED");
        VehProduction production = new VehProduction();
        production.setId(801L);
        production.setVehicleId(80L);
        production.setStageStatus("CONFIRMED");
        production.setConfirmedBy("original-confirmer");
        production.setConfirmedAt(LocalDateTime.of(2026, 1, 2, 3, 4));
        production.setEngineNumber("OLD");
        when(vehicleMapper.selectByIdForUpdate(80L)).thenReturn(vehicle);
        when(vehProductionMapper.selectByIdForUpdate(801L)).thenReturn(production);
        stubActiveMasterData();

        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        ProductionCorrection change = validProduction(801L);
        change.setEngineNumber("NEW");
        request.setProduction(change);
        service.updateCorrection(80L, request);

        assertEquals("NEW", production.getEngineNumber());
        assertEquals("CONFIRMED", production.getStageStatus());
        assertEquals("original-confirmer", production.getConfirmedBy());
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4), production.getConfirmedAt());
        assertEquals("COMPLETED", vehicle.getLifecycleStage());
        verify(vehProductionMapper).updateById(production);
        ArgumentCaptor<CorrectionDiff> captor = ArgumentCaptor.forClass(CorrectionDiff.class);
        verify(auditService).recordSuccess(captor.capture());
        assertEquals("OLD", captor.getValue().getBefore().get("production:801").get("engineNumber"));
        assertEquals("NEW", captor.getValue().getAfter().get("production:801").get("engineNumber"));
    }

    @Test
    void updateRejectsStageRecordOwnedByAnotherVehicle() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehPayment payment = new VehPayment();
        payment.setId(802L);
        payment.setVehicleId(81L);
        when(vehPaymentMapper.selectByIdForUpdate(802L)).thenReturn(payment);
        VehicleCorrectionUpdateRequest request = requestWithPayment(802L, "PAID");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.CORRECTION_RECORD_MISMATCH.getCode(), ex.getCode());
        verify(vehPaymentMapper, never()).updateById(any());
        verify(auditService, never()).recordSuccess(any());
    }

    @Test
    void updateInvoicesPreservesSequenceAndRejectsMissingRecord() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehInvoice first = invoice(803L, 80L, 1);
        when(vehInvoiceMapper.selectByIdForUpdate(803L)).thenReturn(first);
        when(vehInvoiceMapper.selectByIdForUpdate(804L)).thenReturn(null);
        when(statusLabelService.dictLabels("invoice_status"))
                .thenReturn(Map.of("INVOICED", "Formal invoice"));
        VehicleCorrectionUpdateRequest request = requestWithInvoices(
                invoiceChange(803L), invoiceChange(804L));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.STAGE_DATA_NOT_FOUND.getCode(), ex.getCode());
        assertEquals(1, first.getInvoiceSeq());
        verify(auditService, never()).recordSuccess(any());
    }

    @Test
    void updateRejectsDuplicateInvoiceIdsBeforeLockingAnyInvoice() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehicleCorrectionUpdateRequest request = requestWithInvoices(
                invoiceChange(803L), invoiceChange(803L));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehInvoiceMapper, never()).selectByIdForUpdate(any());
    }

    @Test
    void updateRejectsMissingVehicle() {
        when(vehicleMapper.selectByIdForUpdate(80L)).thenReturn(null);
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        InboundCorrection inbound = new InboundCorrection();
        inbound.setId(808L);
        request.setInbound(inbound);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND.getCode(), ex.getCode());
        verify(auditService, never()).recordSuccess(any());
    }

    @Test
    void updateRejectsRequestWithNoSectionsBeforeLockOrAudit() {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehicleMapper, never()).selectByIdForUpdate(any());
        verify(auditService, never()).recordSuccess(any());
    }

    @Test
    void updateRejectsEmptyInvoiceListWithoutOtherSectionsBeforeLockOrAudit() {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setInvoices(List.of());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehicleMapper, never()).selectByIdForUpdate(any());
        verify(auditService, never()).recordSuccess(any());
    }

    @Test
    void updateConfirmedInboundRejectsMissingSaicBuyOffDateWithoutUpdateOrAudit() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehInbound inbound = new VehInbound();
        protectedRecord(inbound, 808L, 80L);
        when(vehInboundMapper.selectByIdForUpdate(808L)).thenReturn(inbound);
        InboundCorrection change = new InboundCorrection();
        change.setId(808L);
        change.setDateToStorageYard(LocalDate.of(2026, 7, 18));
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setInbound(change);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehInboundMapper, never()).updateById(any());
        verify(auditService, never()).recordSuccess(any());
    }

    @Test
    void updateConfirmedInboundRejectsMissingStorageDateWithoutUpdateOrAudit() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehInbound inbound = new VehInbound();
        protectedRecord(inbound, 808L, 80L);
        when(vehInboundMapper.selectByIdForUpdate(808L)).thenReturn(inbound);
        InboundCorrection change = new InboundCorrection();
        change.setId(808L);
        change.setSaicBuyOffDate(LocalDate.of(2026, 7, 17));
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setInbound(change);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehInboundMapper, never()).updateById(any());
        verify(auditService, never()).recordSuccess(any());
    }

    @Test
    void updateConfirmedDeliveryRejectsMissingReceivedDateWithoutUpdateOrAudit() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehDelivery delivery = new VehDelivery();
        protectedRecord(delivery, 806L, 80L);
        when(vehDeliveryMapper.selectByIdForUpdate(806L)).thenReturn(delivery);
        VehicleCorrectionUpdateRequest request = requestWithDelivery(806L, "DELIVERED");
        request.getDelivery().setReceivedDate(null);
        request.getDelivery().setDeliveryStatus(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehDeliveryMapper, never()).updateById(any());
        verify(auditService, never()).recordSuccess(any());
    }

    @Test
    void updateDraftInboundAndDeliveryKeepsNullableDateBehavior() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehInbound inbound = new VehInbound();
        inbound.setId(808L);
        inbound.setVehicleId(80L);
        inbound.setStageStatus("DRAFT");
        when(vehInboundMapper.selectByIdForUpdate(808L)).thenReturn(inbound);
        VehDelivery delivery = new VehDelivery();
        delivery.setId(806L);
        delivery.setVehicleId(80L);
        delivery.setStageStatus("DRAFT");
        when(vehDeliveryMapper.selectByIdForUpdate(806L)).thenReturn(delivery);
        when(statusLabelService.dictLabels("delivery_status"))
                .thenReturn(Map.of("DELIVERED", "Delivered"));
        InboundCorrection inboundChange = new InboundCorrection();
        inboundChange.setId(808L);
        DeliveryCorrection deliveryChange = new DeliveryCorrection();
        deliveryChange.setId(806L);
        deliveryChange.setDeliveryStatus("DELIVERED");
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setInbound(inboundChange);
        request.setDelivery(deliveryChange);

        service.updateCorrection(80L, request);

        verify(vehInboundMapper).updateById(inbound);
        verify(vehDeliveryMapper).updateById(delivery);
        verify(auditService).recordSuccess(any());
    }

    @Test
    void updateRejectsInactiveProductionMasterData() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehProduction production = new VehProduction();
        production.setId(801L);
        production.setVehicleId(80L);
        when(vehProductionMapper.selectByIdForUpdate(801L)).thenReturn(production);
        doReturn(0L).when(vehicleModelMapper).selectCount(any());
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setProduction(validProduction(801L));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehProductionMapper, never()).updateById(any());
    }

    @Test
    void updateRejectsNullRequiredMasterDataIdEvenWithoutControllerValidation() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehProduction production = new VehProduction();
        production.setId(801L);
        production.setVehicleId(80L);
        when(vehProductionMapper.selectByIdForUpdate(801L)).thenReturn(production);
        ProductionCorrection productionChange = validProduction(801L);
        productionChange.setModelId(null);
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setProduction(productionChange);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehicleModelMapper, never()).selectCount(any());
    }

    @Test
    void updateRejectsInactiveDealer() {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehAllocation allocation = new VehAllocation();
        allocation.setId(805L);
        allocation.setVehicleId(80L);
        when(vehAllocationMapper.selectByIdForUpdate(805L)).thenReturn(allocation);
        doReturn(0L).when(dealerMapper).selectCount(any());
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setAllocation(allocationChange(805L, "SOLD"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehAllocationMapper, never()).updateById(any());
    }

    @Test
    void updateRejectsInvalidDictionaryValues() {
        assertInvalidDictionary(requestWithAllocation(805L, "INVALID"),
                "sales_status", () -> {
                    VehAllocation record = new VehAllocation();
                    record.setId(805L);
                    record.setVehicleId(80L);
                    when(vehAllocationMapper.selectByIdForUpdate(805L)).thenReturn(record);
                    doReturn(1L).when(dealerMapper).selectCount(any());
                });
        assertInvalidDictionary(requestWithInvoices(invoiceChange(803L)),
                "invoice_status", () -> {
                    VehInvoice record = invoice(803L, 80L, 1);
                    when(vehInvoiceMapper.selectByIdForUpdate(803L)).thenReturn(record);
                });
        assertInvalidDictionary(requestWithPayment(802L, "INVALID"),
                "payment_status", () -> {
                    VehPayment record = new VehPayment();
                    record.setId(802L);
                    record.setVehicleId(80L);
                    when(vehPaymentMapper.selectByIdForUpdate(802L)).thenReturn(record);
                });
        assertInvalidDictionary(requestWithDelivery(806L, "INVALID"),
                "delivery_status", () -> {
                    VehDelivery record = new VehDelivery();
                    record.setId(806L);
                    record.setVehicleId(80L);
                    when(vehDeliveryMapper.selectByIdForUpdate(806L)).thenReturn(record);
                });
        assertInvalidDictionary(requestWithRegistration(807L, "INVALID"),
                "drosstech_status", () -> {
                    VehRegistration record = new VehRegistration();
                    record.setId(807L);
                    record.setVehicleId(80L);
                    when(vehRegistrationMapper.selectByIdForUpdate(807L)).thenReturn(record);
                });
    }

    @Test
    void updateRejectsDeliveryArrivalBeforeDeparture() {
        stubDelivery(806L);
        DeliveryCorrection delivery = deliveryChange(806L, "DELIVERED");
        delivery.setEtdToDealer(LocalDate.of(2026, 7, 20));
        delivery.setEtaToDealer(LocalDate.of(2026, 7, 19));
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setDelivery(delivery);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehDeliveryMapper, never()).updateById(any());
    }

    @Test
    void updateRejectsUnknownTrollyType() {
        stubDelivery(806L);
        DeliveryCorrection delivery = deliveryChange(806L, "DELIVERED");
        delivery.setTrollyType("8 units");
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setDelivery(delivery);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehDeliveryMapper, never()).updateById(any());
    }

    @Test
    void updateAllRemainingStagesChangesOnlyWhitelistedBusinessFields() {
        Vehicle vehicle = vehicle(80L, "VIN00000000000080", "COMPLETED");
        when(vehicleMapper.selectByIdForUpdate(80L)).thenReturn(vehicle);

        VehInbound inbound = new VehInbound();
        protectedRecord(inbound, 808L, 80L);
        when(vehInboundMapper.selectByIdForUpdate(808L)).thenReturn(inbound);
        VehAllocation allocation = new VehAllocation();
        protectedRecord(allocation, 805L, 80L);
        when(vehAllocationMapper.selectByIdForUpdate(805L)).thenReturn(allocation);
        VehPayment payment = new VehPayment();
        protectedRecord(payment, 802L, 80L);
        when(vehPaymentMapper.selectByIdForUpdate(802L)).thenReturn(payment);
        VehDelivery delivery = new VehDelivery();
        protectedRecord(delivery, 806L, 80L);
        when(vehDeliveryMapper.selectByIdForUpdate(806L)).thenReturn(delivery);
        VehRegistration registration = new VehRegistration();
        protectedRecord(registration, 807L, 80L);
        when(vehRegistrationMapper.selectByIdForUpdate(807L)).thenReturn(registration);
        doReturn(1L).when(dealerMapper).selectCount(any());
        when(statusLabelService.dictLabels("sales_status")).thenReturn(Map.of("SOLD", "Sold"));
        when(statusLabelService.dictLabels("payment_status")).thenReturn(Map.of("PAID", "Paid"));
        when(statusLabelService.dictLabels("delivery_status")).thenReturn(Map.of("DELIVERED", "Delivered"));
        when(statusLabelService.dictLabels("drosstech_status")).thenReturn(Map.of("DONE", "Done"));

        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        InboundCorrection inboundChange = new InboundCorrection();
        inboundChange.setId(808L);
        inboundChange.setSaicBuyOffDate(LocalDate.of(2026, 1, 1));
        inboundChange.setDateToStorageYard(LocalDate.of(2026, 1, 2));
        inboundChange.setRemark2("inbound-updated");
        request.setInbound(inboundChange);
        request.setAllocation(allocationChange(805L, "SOLD"));
        request.setPayment(paymentChange(802L, "PAID"));
        request.setDelivery(deliveryChange(806L, "DELIVERED"));
        request.setRegistration(registrationChange(807L, "DONE"));

        service.updateCorrection(80L, request);

        assertEquals("inbound-updated", inbound.getRemark2());
        assertEquals("SOLD", allocation.getSalesStatus());
        assertEquals("PAID", payment.getPaymentStatus());
        assertEquals("DELIVERED", delivery.getDeliveryStatus());
        assertEquals("DONE", registration.getDrosstechStatus());
        assertProtectedFields(inbound);
        assertProtectedFields(allocation);
        assertProtectedFields(payment);
        assertProtectedFields(delivery);
        assertProtectedFields(registration);
        assertEquals("COMPLETED", vehicle.getLifecycleStage());
        verify(auditService).recordSuccess(any());
    }

    private void assertInvalidDictionary(VehicleCorrectionUpdateRequest request,
                                         String dictCode,
                                         Runnable stageStub) {
        org.mockito.Mockito.reset(vehicleMapper, statusLabelService, vehAllocationMapper,
                vehInvoiceMapper, vehPaymentMapper, vehDeliveryMapper, vehRegistrationMapper,
                dealerMapper, auditService);
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        stageStub.run();
        when(statusLabelService.dictLabels(dictCode)).thenReturn(Map.of("VALID", "Valid"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCorrection(80L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(auditService, never()).recordSuccess(any());
    }

    private void stubDelivery(Long id) {
        when(vehicleMapper.selectByIdForUpdate(80L))
                .thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
        VehDelivery record = new VehDelivery();
        record.setId(id);
        record.setVehicleId(80L);
        when(vehDeliveryMapper.selectByIdForUpdate(id)).thenReturn(record);
    }

    private void stubActiveMasterData() {
        doReturn(1L).when(vehicleModelMapper).selectCount(any());
        doReturn(1L).when(exteriorColorMapper).selectCount(any());
        doReturn(1L).when(interiorColorMapper).selectCount(any());
    }

    private Vehicle vehicle(Long id, String vin, String lifecycleStage) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setVin(vin);
        vehicle.setLifecycleStage(lifecycleStage);
        return vehicle;
    }

    private ProductionCorrection validProduction(Long id) {
        ProductionCorrection change = new ProductionCorrection();
        change.setId(id);
        change.setModelId(11L);
        change.setExteriorColorId(12L);
        change.setInteriorColorId(13L);
        change.setEngineNumber("ENGINE-NEW");
        change.setYearMake("2026");
        change.setMaterial("MATERIAL");
        change.setShipment("SHIPMENT");
        change.setBatch("BATCH");
        change.setOfflineEpmbDate(LocalDate.of(2026, 1, 1));
        change.setEpmbOkDate(LocalDate.of(2026, 1, 2));
        change.setRemark1("remark");
        return change;
    }

    private VehicleCorrectionUpdateRequest requestWithPayment(Long id, String status) {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setPayment(paymentChange(id, status));
        return request;
    }

    private PaymentCorrection paymentChange(Long id, String status) {
        PaymentCorrection change = new PaymentCorrection();
        change.setId(id);
        change.setPaymentDate(LocalDate.of(2026, 1, 1));
        change.setPaymentStatus(status);
        return change;
    }

    private VehicleCorrectionUpdateRequest requestWithInvoices(InvoiceCorrection... changes) {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setInvoices(List.of(changes));
        return request;
    }

    private VehInvoice invoice(Long id, Long vehicleId, Integer sequence) {
        VehInvoice invoice = new VehInvoice();
        invoice.setId(id);
        invoice.setVehicleId(vehicleId);
        invoice.setInvoiceSeq(sequence);
        invoice.setStageStatus("CONFIRMED");
        invoice.setConfirmedBy("original-confirmer");
        return invoice;
    }

    private InvoiceCorrection invoiceChange(Long id) {
        InvoiceCorrection change = new InvoiceCorrection();
        change.setId(id);
        change.setInvoiceType("INVOICED");
        change.setInvoiceNo("INV-" + id);
        change.setInvoiceDate(LocalDate.of(2026, 1, 1));
        return change;
    }

    private VehicleCorrectionUpdateRequest requestWithAllocation(Long id, String status) {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setAllocation(allocationChange(id, status));
        return request;
    }

    private AllocationCorrection allocationChange(Long id, String status) {
        AllocationCorrection change = new AllocationCorrection();
        change.setId(id);
        change.setDealerId(21L);
        change.setSalesStatus(status);
        return change;
    }

    private VehicleCorrectionUpdateRequest requestWithDelivery(Long id, String status) {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setDelivery(deliveryChange(id, status));
        return request;
    }

    private DeliveryCorrection deliveryChange(Long id, String status) {
        DeliveryCorrection change = new DeliveryCorrection();
        change.setId(id);
        change.setEtdToDealer(LocalDate.of(2026, 1, 1));
        change.setEtaToDealer(LocalDate.of(2026, 1, 2));
        change.setTrollyType("4 units");
        change.setReceivedDate(LocalDate.of(2026, 1, 3));
        change.setDeliveryStatus(status);
        return change;
    }

    private VehicleCorrectionUpdateRequest requestWithRegistration(Long id, String status) {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setRegistration(registrationChange(id, status));
        return request;
    }

    private RegistrationCorrection registrationChange(Long id, String status) {
        RegistrationCorrection change = new RegistrationCorrection();
        change.setId(id);
        change.setDrosstechStatus(status);
        return change;
    }

    private void protectedRecord(VehInbound record, Long id, Long vehicleId) {
        record.setId(id);
        record.setVehicleId(vehicleId);
        record.setStageStatus("CONFIRMED");
        record.setConfirmedBy("original-confirmer");
    }

    private void protectedRecord(VehAllocation record, Long id, Long vehicleId) {
        record.setId(id);
        record.setVehicleId(vehicleId);
        record.setStageStatus("CONFIRMED");
        record.setConfirmedBy("original-confirmer");
    }

    private void protectedRecord(VehPayment record, Long id, Long vehicleId) {
        record.setId(id);
        record.setVehicleId(vehicleId);
        record.setStageStatus("CONFIRMED");
        record.setConfirmedBy("original-confirmer");
    }

    private void protectedRecord(VehDelivery record, Long id, Long vehicleId) {
        record.setId(id);
        record.setVehicleId(vehicleId);
        record.setStageStatus("CONFIRMED");
        record.setConfirmedBy("original-confirmer");
    }

    private void protectedRecord(VehRegistration record, Long id, Long vehicleId) {
        record.setId(id);
        record.setVehicleId(vehicleId);
        record.setStageStatus("CONFIRMED");
        record.setConfirmedBy("original-confirmer");
    }

    private void assertProtectedFields(VehInbound record) {
        assertEquals("CONFIRMED", record.getStageStatus());
        assertEquals("original-confirmer", record.getConfirmedBy());
    }

    private void assertProtectedFields(VehAllocation record) {
        assertEquals("CONFIRMED", record.getStageStatus());
        assertEquals("original-confirmer", record.getConfirmedBy());
    }

    private void assertProtectedFields(VehPayment record) {
        assertEquals("CONFIRMED", record.getStageStatus());
        assertEquals("original-confirmer", record.getConfirmedBy());
    }

    private void assertProtectedFields(VehDelivery record) {
        assertEquals("CONFIRMED", record.getStageStatus());
        assertEquals("original-confirmer", record.getConfirmedBy());
    }

    private void assertProtectedFields(VehRegistration record) {
        assertEquals("CONFIRMED", record.getStageStatus());
        assertEquals("original-confirmer", record.getConfirmedBy());
    }
}

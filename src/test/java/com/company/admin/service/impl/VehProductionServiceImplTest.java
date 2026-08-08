package com.company.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.ProductionQueryRequest;
import com.company.admin.dto.request.ProductionSaveRequest;
import com.company.admin.dto.response.ProductionResponse;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.entity.Vehicle;
import com.company.admin.entity.VehProduction;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.mapper.VehProductionMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.BusinessStatusLabelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehProductionServiceImplTest {

    @Mock
    private VehicleMapper vehicleMapper;

    @Mock
    private VehProductionMapper vehProductionMapper;

    @Mock
    private LifecycleService lifecycleService;

    @Mock
    private BusinessStatusLabelService statusLabelService;

    @InjectMocks
    private VehProductionServiceImpl service;

    @Test
    void pageProductionsPassesFiltersAndAppliesChineseLabels() {
        ProductionQueryRequest request = new ProductionQueryRequest();
        request.setPageNum(2);
        request.setPageSize(5);
        request.setVin("LSJW");

        VehicleListResponse record = new VehicleListResponse();
        record.setLifecycleStage("PENDING_OFFLINE");
        record.setProductionStatus("DRAFT");
        Page<VehicleListResponse> page = new Page<>(2, 5);
        page.setRecords(java.util.List.of(record));
        page.setTotal(11);
        when(vehProductionMapper.selectProductionPage(any(), eq(request))).thenReturn(page);
        when(statusLabelService.lifecycleStageLabel("PENDING_OFFLINE")).thenReturn("草稿（新录入）");
        when(statusLabelService.stageStatusLabel("DRAFT")).thenReturn("草稿");

        PageResult<VehicleListResponse> result = service.pageProductions(request);

        ArgumentCaptor<Page<VehicleListResponse>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(vehProductionMapper).selectProductionPage(pageCaptor.capture(), eq(request));
        assertEquals(2, pageCaptor.getValue().getCurrent());
        assertEquals(5, pageCaptor.getValue().getSize());
        assertEquals(11, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals("草稿（新录入）", result.getList().get(0).getLifecycleStageLabel());
        assertEquals("草稿", result.getList().get(0).getProductionStatusLabel());
    }

    @Test
    void createProductionRejectsDuplicateVin() {
        ProductionSaveRequest request = sampleRequest();
        when(vehicleMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createProduction(request));

        assertEquals(ErrorCode.VIN_DUPLICATE.getCode(), exception.getCode());
        verifyNoInteractions(vehProductionMapper);
    }

    @Test
    void createProductionCreatesVehicleAndDraftProduction() {
        ProductionSaveRequest request = sampleRequest();
        when(vehicleMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(99L);
            return 1;
        }).when(vehicleMapper).insert(any(Vehicle.class));

        Long vehicleId = service.createProduction(request);

        assertEquals(99L, vehicleId);
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleMapper).insert(vehicleCaptor.capture());
        assertEquals("LSJW56U95RG000001", vehicleCaptor.getValue().getVin());
        assertEquals(LifecycleStage.PENDING_OFFLINE.name(), vehicleCaptor.getValue().getLifecycleStage());

        ArgumentCaptor<VehProduction> productionCaptor = ArgumentCaptor.forClass(VehProduction.class);
        verify(vehProductionMapper).insert(productionCaptor.capture());
        VehProduction production = productionCaptor.getValue();
        assertEquals(99L, production.getVehicleId());
        assertEquals(StageStatus.DRAFT.name(), production.getStageStatus());
        assertEquals(1L, production.getModelId());
        assertEquals("ENGINE-001", production.getEngineNumber());
        assertEquals(LocalDate.of(2026, 6, 29), production.getOfflineEpmbDate());
    }

    @Test
    void confirmProductionLocksProductionAndAdvancesLifecycle() {
        VehProduction production = new VehProduction();
        production.setId(10L);
        production.setVehicleId(99L);
        production.setStageStatus(StageStatus.DRAFT.name());
        when(vehProductionMapper.selectOne(any())).thenReturn(production);
        doAnswer(invocation -> {
            Runnable lockAction = invocation.getArgument(4);
            lockAction.run();
            return null;
        }).when(lifecycleService).confirmAndAdvance(
                eq(99L),
                eq(StageStatus.DRAFT.name()),
                eq(LifecycleStage.PENDING_OFFLINE),
                eq(LifecycleStage.PENDING_INBOUND),
                any(Runnable.class));

        service.confirmProduction(99L);

        ArgumentCaptor<VehProduction> captor = ArgumentCaptor.forClass(VehProduction.class);
        verify(vehProductionMapper).updateById(captor.capture());
        assertEquals(StageStatus.CONFIRMED.name(), captor.getValue().getStageStatus());
        assertNotNull(captor.getValue().getConfirmedAt());
    }

    @Test
    void getProductionAddsChineseStageStatusLabel() {
        VehProduction production = new VehProduction();
        production.setVehicleId(99L);
        production.setStageStatus(StageStatus.DRAFT.name());
        when(vehProductionMapper.selectOne(any())).thenReturn(production);
        when(statusLabelService.stageStatusLabel("DRAFT")).thenReturn("草稿");

        ProductionResponse response = service.getProduction(99L);

        assertEquals("DRAFT", response.getStageStatus());
        assertEquals("草稿", response.getStageStatusLabel());
    }

    private ProductionSaveRequest sampleRequest() {
        ProductionSaveRequest request = new ProductionSaveRequest();
        request.setVin("LSJW56U95RG000001");
        request.setModelId(1L);
        request.setExteriorColorId(2L);
        request.setInteriorColorId(3L);
        request.setEngineNumber("ENGINE-001");
        request.setYearMake("2026");
        request.setMaterial("material");
        request.setShipment("shipment");
        request.setBatch("batch");
        request.setOfflineEpmbDate(LocalDate.of(2026, 6, 29));
        request.setEpmbOkDate(LocalDate.of(2026, 6, 30));
        request.setRemark1("remark");
        return request;
    }
}

package com.company.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.entity.Vehicle;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.VehiclePanoramaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private VehicleCorrectionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VehicleCorrectionServiceImpl(
                vehicleMapper, vehiclePanoramaService, statusLabelService);
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
}

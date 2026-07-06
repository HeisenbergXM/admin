package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.AllocationSaveRequest;
import com.company.admin.dto.response.AllocationResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.VehAllocation;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehAllocationMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehicleBasicService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehAllocationServiceImplTest {

    @Mock
    private VehAllocationMapper vehAllocationMapper;

    @Mock
    private LifecycleService lifecycleService;

    @Mock
    private VehicleBasicService vehicleBasicService;

    @InjectMocks
    private VehAllocationServiceImpl service;

    @Test
    void createAllocationChecksPendingAllocationAndStoresDraft() {
        doAnswer(invocation -> {
            VehAllocation allocation = invocation.getArgument(0);
            allocation.setId(8L);
            return 1;
        }).when(vehAllocationMapper).insert(any(VehAllocation.class));

        Long id = service.createAllocation(99L, saveRequest());

        assertEquals(8L, id);
        verify(lifecycleService).assertStage(99L, LifecycleStage.PENDING_ALLOCATION);
        ArgumentCaptor<VehAllocation> captor = ArgumentCaptor.forClass(VehAllocation.class);
        verify(vehAllocationMapper).insert(captor.capture());
        assertEquals(99L, captor.getValue().getVehicleId());
        assertEquals(StageStatus.DRAFT.name(), captor.getValue().getStageStatus());
        assertEquals(3L, captor.getValue().getDealerId());
    }

    @Test
    void confirmAllocationLocksAndAdvancesLifecycle() {
        VehAllocation allocation = draftAllocation();
        when(vehAllocationMapper.selectOne(any())).thenReturn(allocation);
        doAnswer(invocation -> {
            Runnable lockAction = invocation.getArgument(4);
            lockAction.run();
            return null;
        }).when(lifecycleService).confirmAndAdvance(
                eq(99L),
                eq(StageStatus.DRAFT.name()),
                eq(LifecycleStage.PENDING_ALLOCATION),
                eq(LifecycleStage.PENDING_INVOICE),
                any(Runnable.class));

        service.confirmAllocation(99L);

        ArgumentCaptor<VehAllocation> captor = ArgumentCaptor.forClass(VehAllocation.class);
        verify(vehAllocationMapper).updateById(captor.capture());
        assertEquals(StageStatus.CONFIRMED.name(), captor.getValue().getStageStatus());
        assertNotNull(captor.getValue().getConfirmedAt());
    }

    @Test
    void updateAllocationRejectsConfirmedRecord() {
        VehAllocation allocation = draftAllocation();
        allocation.setStageStatus(StageStatus.CONFIRMED.name());
        when(vehAllocationMapper.selectOne(any())).thenReturn(allocation);
        doThrow(new BusinessException(ErrorCode.STAGE_ALREADY_CONFIRMED))
                .when(lifecycleService).assertNotConfirmed(StageStatus.CONFIRMED.name());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateAllocation(99L, saveRequest()));

        assertEquals(ErrorCode.STAGE_ALREADY_CONFIRMED.getCode(), exception.getCode());
    }

    @Test
    void getAllocationReturnsPendingVehicleBasicInfoWhenDraftDoesNotExist() {
        when(vehAllocationMapper.selectOne(any())).thenReturn(null);
        VehicleBasicInfo basicInfo = new VehicleBasicInfo();
        basicInfo.setId(99L);
        basicInfo.setVin("VIN00000000000099");
        basicInfo.setLifecycleStage(LifecycleStage.PENDING_ALLOCATION.name());
        basicInfo.setModelName("MG4 EV");
        basicInfo.setExteriorColorName("Moon White");
        basicInfo.setYearMake("2026");
        when(vehicleBasicService.getBasicInfo(99L)).thenReturn(basicInfo);

        AllocationResponse response = service.getAllocation(99L);

        assertEquals(99L, response.getVehicleId());
        assertEquals("VIN00000000000099", response.getVin());
        assertEquals(LifecycleStage.PENDING_ALLOCATION.name(), response.getStageStatus());
        assertEquals(LifecycleStage.PENDING_ALLOCATION.name(), response.getLifecycleStage());
        assertEquals("MG4 EV", response.getModelName());
        assertEquals("Moon White", response.getExteriorColorName());
        assertEquals("2026", response.getYearMake());
    }

    private AllocationSaveRequest saveRequest() {
        AllocationSaveRequest request = new AllocationSaveRequest();
        request.setAllocatedDate(LocalDate.of(2026, 7, 2));
        request.setDealerId(3L);
        request.setSalesStatus("ALLOCATED");
        request.setRemark3("remark");
        return request;
    }

    private VehAllocation draftAllocation() {
        VehAllocation allocation = new VehAllocation();
        allocation.setId(8L);
        allocation.setVehicleId(99L);
        allocation.setStageStatus(StageStatus.DRAFT.name());
        return allocation;
    }
}

package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.DispatchListSaveRequest;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.DispatchList;
import com.company.admin.entity.WaybillDealer;
import com.company.admin.entity.WaybillDealerVin;
import com.company.admin.entity.Waybill;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.OrderStatus;
import com.company.admin.mapper.DispatchListMapper;
import com.company.admin.mapper.WaybillDealerMapper;
import com.company.admin.mapper.WaybillDealerVinMapper;
import com.company.admin.mapper.WaybillMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.VehicleBasicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchListServiceImplTest {

    @Mock
    private DispatchListMapper dispatchListMapper;

    @Mock
    private WaybillMapper waybillMapper;

    @Mock
    private WaybillDealerMapper waybillDealerMapper;

    @Mock
    private WaybillDealerVinMapper waybillDealerVinMapper;

    @Mock
    private LifecycleService lifecycleService;

    @Mock
    private VehicleBasicService vehicleBasicService;

    @Mock
    private BusinessStatusLabelService statusLabelService;

    @InjectMocks
    private DispatchListServiceImpl service;

    @Test
    void createDispatchListStoresDraftHeader() {
        doAnswer(invocation -> {
            DispatchList dispatchList = invocation.getArgument(0);
            dispatchList.setId(15L);
            return 1;
        }).when(dispatchListMapper).insert(any(DispatchList.class));

        Long id = service.createDispatchList(saveRequest());

        assertEquals(15L, id);
        ArgumentCaptor<DispatchList> captor = ArgumentCaptor.forClass(DispatchList.class);
        verify(dispatchListMapper).insert(captor.capture());
        assertEquals("DL-001", captor.getValue().getDispatchNo());
        assertEquals(OrderStatus.DRAFT.name(), captor.getValue().getListStatus());
    }

    @Test
    void addVinRejectsVehicleAllocatedToDifferentDealer() {
        WaybillDealer dealerRow = dealerRow(3L);
        VehicleBasicInfo info = vehicleInfo(4L);
        when(waybillDealerMapper.selectById(22L)).thenReturn(dealerRow);
        when(vehicleBasicService.getBasicInfo(99L)).thenReturn(info);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.addVin(22L, 99L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), exception.getCode());
        verify(lifecycleService).assertStage(99L, LifecycleStage.PENDING_DELIVERY);
    }

    @Test
    void confirmDealerRowLocksRowAndAdvancesOnlyRowVehicles() {
        WaybillDealer dealerRow = dealerRow(3L);
        when(waybillDealerMapper.selectById(22L)).thenReturn(dealerRow);
        when(waybillDealerVinMapper.selectByDealerRowId(22L)).thenReturn(List.of(vin(99L), vin(100L)));
        doAnswer(invocation -> {
            Runnable lockAction = invocation.getArgument(4);
            lockAction.run();
            return null;
        }).when(lifecycleService).confirmAndAdvance(
                eq(99L),
                eq(OrderStatus.DRAFT.name()),
                eq(LifecycleStage.PENDING_DELIVERY),
                eq(LifecycleStage.PENDING_REGISTRATION),
                any(Runnable.class));

        service.confirmDealerRow(22L);

        verify(lifecycleService).assertStage(100L, LifecycleStage.PENDING_DELIVERY);
        verify(lifecycleService).advanceStage(100L, LifecycleStage.PENDING_DELIVERY, LifecycleStage.PENDING_REGISTRATION);
        ArgumentCaptor<WaybillDealer> captor = ArgumentCaptor.forClass(WaybillDealer.class);
        verify(waybillDealerMapper).updateById(captor.capture());
        assertEquals(OrderStatus.CONFIRMED.name(), captor.getValue().getRowStatus());
        assertNotNull(captor.getValue().getConfirmedAt());
    }

    @Test
    void getDetailAddsChineseDispatchAndDealerStatusLabels() {
        DispatchList dispatchList = new DispatchList();
        dispatchList.setId(15L);
        dispatchList.setListStatus(OrderStatus.DRAFT.name());
        WaybillDealer dealer = dealerRow(3L);
        dealer.setWaybillId(7L);
        dealer.setDeliveryStatus("IN_TRANSIT");
        Waybill waybill = new Waybill();
        waybill.setId(7L);
        waybill.setDispatchListId(15L);
        when(dispatchListMapper.selectById(15L)).thenReturn(dispatchList);
        when(waybillMapper.selectByDispatchListId(15L)).thenReturn(List.of(waybill));
        when(waybillDealerMapper.selectByDispatchListId(15L)).thenReturn(List.of(dealer));
        when(waybillDealerVinMapper.selectByDealerRowId(22L)).thenReturn(List.of());
        when(vehicleBasicService.getBasicInfoByIds(List.of())).thenReturn(List.of());
        when(statusLabelService.stageStatusLabel("DRAFT")).thenReturn("草稿");
        when(statusLabelService.dictLabel("delivery_status", "IN_TRANSIT")).thenReturn("运输中");

        var response = service.getDetail(15L);

        assertEquals("草稿", response.getListStatusLabel());
    }

    private DispatchListSaveRequest saveRequest() {
        DispatchListSaveRequest request = new DispatchListSaveRequest();
        request.setDispatchNo("DL-001");
        return request;
    }

    private WaybillDealer dealerRow(Long dealerId) {
        WaybillDealer dealer = new WaybillDealer();
        dealer.setId(22L);
        dealer.setDealerId(dealerId);
        dealer.setRowStatus(OrderStatus.DRAFT.name());
        return dealer;
    }

    private VehicleBasicInfo vehicleInfo(Long dealerId) {
        VehicleBasicInfo info = new VehicleBasicInfo();
        info.setId(99L);
        info.setDealerId(dealerId);
        return info;
    }

    private WaybillDealerVin vin(Long vehicleId) {
        WaybillDealerVin vin = new WaybillDealerVin();
        vin.setWaybillDealerId(22L);
        vin.setVehicleId(vehicleId);
        return vin;
    }
}

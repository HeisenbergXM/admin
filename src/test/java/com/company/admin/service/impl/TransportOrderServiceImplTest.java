package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.TransportOrderItemRequest;
import com.company.admin.dto.request.TransportOrderSaveRequest;
import com.company.admin.entity.TransportOrder;
import com.company.admin.entity.TransportOrderItem;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.OrderStatus;
import com.company.admin.mapper.TransportOrderItemMapper;
import com.company.admin.mapper.TransportOrderMapper;
import com.company.admin.service.LifecycleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
class TransportOrderServiceImplTest {

    @Mock
    private TransportOrderMapper transportOrderMapper;

    @Mock
    private TransportOrderItemMapper transportOrderItemMapper;

    @Mock
    private LifecycleService lifecycleService;

    @InjectMocks
    private TransportOrderServiceImpl service;

    @Test
    void createOrderStoresDraftHeader() {
        TransportOrderSaveRequest request = saveRequest();
        doAnswer(invocation -> {
            TransportOrder order = invocation.getArgument(0);
            order.setId(10L);
            return 1;
        }).when(transportOrderMapper).insert(any(TransportOrder.class));

        Long id = service.createOrder(request);

        assertEquals(10L, id);
        ArgumentCaptor<TransportOrder> captor = ArgumentCaptor.forClass(TransportOrder.class);
        verify(transportOrderMapper).insert(captor.capture());
        assertEquals("TO-001", captor.getValue().getOrderNo());
        assertEquals(OrderStatus.DRAFT.name(), captor.getValue().getOrderStatus());
    }

    @Test
    void addItemRejectsVehicleOccupiedByAnotherDraftOrder() {
        when(transportOrderMapper.selectById(10L)).thenReturn(draftOrder());
        when(transportOrderItemMapper.countOpenOccupancy(20L, 10L)).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.addItem(10L, itemRequest(20L)));

        assertEquals(ErrorCode.VEHICLE_OCCUPIED_BY_OTHER_ORDER.getCode(), exception.getCode());
    }

    @Test
    void confirmOrderLocksOrderAndAdvancesAllVehicles() {
        TransportOrder order = draftOrder();
        when(transportOrderMapper.selectById(10L)).thenReturn(order);
        when(transportOrderItemMapper.selectByOrderId(10L)).thenReturn(List.of(item(20L), item(21L)));
        doAnswer(invocation -> {
            Runnable lockAction = invocation.getArgument(4);
            lockAction.run();
            return null;
        }).when(lifecycleService).confirmAndAdvance(
                eq(20L),
                eq(OrderStatus.DRAFT.name()),
                eq(LifecycleStage.PENDING_INBOUND),
                eq(LifecycleStage.PENDING_ALLOCATION),
                any(Runnable.class));

        service.confirmOrder(10L);

        verify(lifecycleService).assertStage(21L, LifecycleStage.PENDING_INBOUND);
        verify(lifecycleService).advanceStage(21L, LifecycleStage.PENDING_INBOUND, LifecycleStage.PENDING_ALLOCATION);
        ArgumentCaptor<TransportOrder> captor = ArgumentCaptor.forClass(TransportOrder.class);
        verify(transportOrderMapper).updateById(captor.capture());
        assertEquals(OrderStatus.CONFIRMED.name(), captor.getValue().getOrderStatus());
        assertNotNull(captor.getValue().getConfirmedAt());
    }

    private TransportOrderSaveRequest saveRequest() {
        TransportOrderSaveRequest request = new TransportOrderSaveRequest();
        request.setOrderNo("TO-001");
        request.setDateToStorageYard(LocalDate.of(2026, 6, 30));
        request.setRemark2("remark");
        return request;
    }

    private TransportOrderItemRequest itemRequest(Long vehicleId) {
        TransportOrderItemRequest request = new TransportOrderItemRequest();
        request.setVehicleId(vehicleId);
        request.setSaicBuyOffDate(LocalDate.of(2026, 7, 1));
        return request;
    }

    private TransportOrder draftOrder() {
        TransportOrder order = new TransportOrder();
        order.setId(10L);
        order.setOrderStatus(OrderStatus.DRAFT.name());
        return order;
    }

    private TransportOrderItem item(Long vehicleId) {
        TransportOrderItem item = new TransportOrderItem();
        item.setId(vehicleId + 100);
        item.setTransportOrderId(10L);
        item.setVehicleId(vehicleId);
        return item;
    }
}

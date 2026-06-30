package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.TransportOrderItemRequest;
import com.company.admin.dto.request.TransportOrderQueryRequest;
import com.company.admin.dto.request.TransportOrderSaveRequest;
import com.company.admin.dto.response.TransportOrderDetailResponse;
import com.company.admin.dto.response.TransportOrderListResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.TransportOrder;
import com.company.admin.entity.TransportOrderItem;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.OrderStatus;
import com.company.admin.mapper.TransportOrderItemMapper;
import com.company.admin.mapper.TransportOrderMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.TransportOrderService;
import com.company.admin.service.VehicleBasicService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportOrderServiceImpl implements TransportOrderService {

    private final TransportOrderMapper transportOrderMapper;
    private final TransportOrderItemMapper transportOrderItemMapper;
    private final LifecycleService lifecycleService;
    private final VehicleBasicService vehicleBasicService;

    @Override
    public PageResult<TransportOrderListResponse> pageOrders(TransportOrderQueryRequest request) {
        Page<TransportOrderListResponse> page = transportOrderMapper.selectOrderPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public Long createOrder(TransportOrderSaveRequest request) {
        TransportOrder order = new TransportOrder();
        copyHeader(request, order);
        order.setOrderStatus(OrderStatus.DRAFT.name());
        transportOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    @Transactional
    public void updateOrder(Long id, TransportOrderSaveRequest request) {
        TransportOrder existing = getOrderEntity(id);
        assertDraft(existing);
        TransportOrder update = new TransportOrder();
        copyHeader(request, update);
        update.setId(id);
        transportOrderMapper.updateById(update);
    }

    @Override
    @Transactional
    public Long addItem(Long orderId, TransportOrderItemRequest request) {
        TransportOrder order = getOrderEntity(orderId);
        assertDraft(order);
        lifecycleService.assertStage(request.getVehicleId(), LifecycleStage.PENDING_INBOUND);
        if (transportOrderItemMapper.countOpenOccupancy(request.getVehicleId(), orderId) > 0) {
            throw new BusinessException(ErrorCode.VEHICLE_OCCUPIED_BY_OTHER_ORDER);
        }
        TransportOrderItem item = new TransportOrderItem();
        item.setTransportOrderId(orderId);
        item.setVehicleId(request.getVehicleId());
        item.setSaicBuyOffDate(request.getSaicBuyOffDate());
        transportOrderItemMapper.insert(item);
        return item.getId();
    }

    @Override
    @Transactional
    public void updateItem(Long orderId, Long itemId, TransportOrderItemRequest request) {
        TransportOrder order = getOrderEntity(orderId);
        assertDraft(order);
        TransportOrderItem item = getItemEntity(orderId, itemId);
        item.setSaicBuyOffDate(request.getSaicBuyOffDate());
        transportOrderItemMapper.updateById(item);
    }

    @Override
    @Transactional
    public void removeItem(Long orderId, Long itemId) {
        TransportOrder order = getOrderEntity(orderId);
        assertDraft(order);
        getItemEntity(orderId, itemId);
        transportOrderItemMapper.deleteById(itemId);
    }

    @Override
    @Transactional
    public void confirmOrder(Long id) {
        TransportOrder order = getOrderEntity(id);
        assertDraft(order);
        List<TransportOrderItem> items = transportOrderItemMapper.selectByOrderId(id);
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        TransportOrderItem first = items.get(0);
        lifecycleService.confirmAndAdvance(
                first.getVehicleId(),
                order.getOrderStatus(),
                LifecycleStage.PENDING_INBOUND,
                LifecycleStage.PENDING_ALLOCATION,
                () -> lockOrder(order));

        for (int i = 1; i < items.size(); i++) {
            Long vehicleId = items.get(i).getVehicleId();
            lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_INBOUND);
            lifecycleService.advanceStage(vehicleId, LifecycleStage.PENDING_INBOUND, LifecycleStage.PENDING_ALLOCATION);
        }
    }

    @Override
    public TransportOrderDetailResponse getDetail(Long id) {
        TransportOrder order = getOrderEntity(id);
        List<TransportOrderItem> items = transportOrderItemMapper.selectByOrderId(id);
        List<Long> vehicleIds = items.stream().map(TransportOrderItem::getVehicleId).collect(Collectors.toList());
        Map<Long, VehicleBasicInfo> vehicleMap = vehicleBasicService.getBasicInfoByIds(vehicleIds)
                .stream()
                .collect(Collectors.toMap(VehicleBasicInfo::getId, Function.identity(), (a, b) -> a));

        TransportOrderDetailResponse response = new TransportOrderDetailResponse();
        BeanUtils.copyProperties(order, response);
        response.setItems(items.stream().map(item -> {
            TransportOrderDetailResponse.Item dto = new TransportOrderDetailResponse.Item();
            dto.setId(item.getId());
            dto.setVehicleId(item.getVehicleId());
            dto.setSaicBuyOffDate(item.getSaicBuyOffDate());
            dto.setVehicle(vehicleMap.get(item.getVehicleId()));
            return dto;
        }).collect(Collectors.toList()));
        return response;
    }

    private void copyHeader(TransportOrderSaveRequest request, TransportOrder order) {
        order.setOrderNo(request.getOrderNo());
        order.setDateToStorageYard(request.getDateToStorageYard());
        order.setRemark2(request.getRemark2());
    }

    private TransportOrder getOrderEntity(Long id) {
        TransportOrder order = transportOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.TRANSPORT_ORDER_NOT_FOUND);
        }
        return order;
    }

    private TransportOrderItem getItemEntity(Long orderId, Long itemId) {
        TransportOrderItem item = transportOrderItemMapper.selectOne(new LambdaQueryWrapper<TransportOrderItem>()
                .eq(TransportOrderItem::getId, itemId)
                .eq(TransportOrderItem::getTransportOrderId, orderId));
        if (item == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return item;
    }

    private void assertDraft(TransportOrder order) {
        if (OrderStatus.CONFIRMED.name().equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.STAGE_ALREADY_CONFIRMED);
        }
    }

    private void lockOrder(TransportOrder order) {
        order.setOrderStatus(OrderStatus.CONFIRMED.name());
        order.setConfirmedBy(SecurityUtils.getCurrentUsername());
        order.setConfirmedAt(LocalDateTime.now());
        transportOrderMapper.updateById(order);
    }
}

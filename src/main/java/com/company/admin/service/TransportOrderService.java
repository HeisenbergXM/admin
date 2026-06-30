package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.TransportOrderItemRequest;
import com.company.admin.dto.request.TransportOrderQueryRequest;
import com.company.admin.dto.request.TransportOrderSaveRequest;
import com.company.admin.dto.response.TransportOrderDetailResponse;
import com.company.admin.dto.response.TransportOrderListResponse;

public interface TransportOrderService {

    PageResult<TransportOrderListResponse> pageOrders(TransportOrderQueryRequest request);

    Long createOrder(TransportOrderSaveRequest request);

    void updateOrder(Long id, TransportOrderSaveRequest request);

    Long addItem(Long orderId, TransportOrderItemRequest request);

    void updateItem(Long orderId, Long itemId, TransportOrderItemRequest request);

    void removeItem(Long orderId, Long itemId);

    void confirmOrder(Long id);

    TransportOrderDetailResponse getDetail(Long id);
}

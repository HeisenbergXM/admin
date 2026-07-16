package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DeliveryQueryRequest;
import com.company.admin.dto.request.DeliverySaveRequest;
import com.company.admin.dto.response.DeliveryResponse;

public interface VehDeliveryService {

    PageResult<DeliveryResponse> pageDeliveries(DeliveryQueryRequest request);

    Long createDelivery(Long vehicleId, DeliverySaveRequest request);

    void updateDelivery(Long vehicleId, DeliverySaveRequest request);

    void confirmDelivery(Long vehicleId);

    DeliveryResponse getDelivery(Long vehicleId);
}

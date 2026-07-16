package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.InboundQueryRequest;
import com.company.admin.dto.request.InboundSaveRequest;
import com.company.admin.dto.response.InboundResponse;

public interface VehInboundService {

    PageResult<InboundResponse> pageInbounds(InboundQueryRequest request);

    Long createInbound(Long vehicleId, InboundSaveRequest request);

    void updateInbound(Long vehicleId, InboundSaveRequest request);

    void confirmInbound(Long vehicleId);

    InboundResponse getInbound(Long vehicleId);
}

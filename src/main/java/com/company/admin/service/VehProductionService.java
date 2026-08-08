package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.ProductionQueryRequest;
import com.company.admin.dto.request.ProductionSaveRequest;
import com.company.admin.dto.response.ProductionResponse;
import com.company.admin.dto.response.VehicleListResponse;

public interface VehProductionService {

    PageResult<VehicleListResponse> pageProductions(ProductionQueryRequest request);

    Long createProduction(ProductionSaveRequest request);

    void updateProduction(Long vehicleId, ProductionSaveRequest request);

    void confirmProduction(Long vehicleId);

    ProductionResponse getProduction(Long vehicleId);
}

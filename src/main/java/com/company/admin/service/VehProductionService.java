package com.company.admin.service;

import com.company.admin.dto.request.ProductionSaveRequest;
import com.company.admin.dto.response.ProductionResponse;

public interface VehProductionService {

    Long createProduction(ProductionSaveRequest request);

    void updateProduction(Long vehicleId, ProductionSaveRequest request);

    void confirmProduction(Long vehicleId);

    ProductionResponse getProduction(Long vehicleId);
}

package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.dto.response.VehiclePanoramaResponse;

public interface VehicleCorrectionService {

    PageResult<VehicleListResponse> pageCorrections(VehicleQueryRequest request);

    VehiclePanoramaResponse getCorrection(Long vehicleId);
}

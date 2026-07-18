package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleCorrectionListResponse;
import com.company.admin.dto.response.VehiclePanoramaResponse;

public interface VehicleCorrectionService {

    PageResult<VehicleCorrectionListResponse> pageCorrections(VehicleQueryRequest request);

    byte[] exportCorrections(VehicleQueryRequest request);

    VehiclePanoramaResponse getCorrection(Long vehicleId);

    void updateCorrection(Long vehicleId, VehicleCorrectionUpdateRequest request);
}

package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.VehicleCandidateRequest;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.dto.response.VehicleDetailResponse;
import com.company.admin.dto.response.VehicleListResponse;

import java.util.List;

public interface VehicleService {

    PageResult<VehicleListResponse> pageVehicles(VehicleQueryRequest request);

    VehicleDetailResponse getDetail(Long vehicleId);

    VehicleBasicInfo getBasicInfo(Long vehicleId);

    List<VehicleBasicInfo> listCandidates(VehicleCandidateRequest request);

    boolean checkVinAvailable(String vin);
}

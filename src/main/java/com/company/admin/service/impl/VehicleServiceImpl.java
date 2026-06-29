package com.company.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.VehicleCandidateRequest;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.ProductionResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.dto.response.VehicleDetailResponse;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.service.VehicleBasicService;
import com.company.admin.service.VehicleService;
import com.company.admin.service.VehProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleMapper vehicleMapper;
    private final VehicleBasicService vehicleBasicService;
    private final VehProductionService vehProductionService;

    @Override
    public PageResult<VehicleListResponse> pageVehicles(VehicleQueryRequest request) {
        Page<VehicleListResponse> page = vehicleMapper.selectVehiclePage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    public VehicleDetailResponse getDetail(Long vehicleId) {
        VehicleDetailResponse response = new VehicleDetailResponse();
        response.setBasicInfo(vehicleBasicService.getBasicInfo(vehicleId));
        ProductionResponse production = vehProductionService.getProduction(vehicleId);
        response.setProduction(production);
        return response;
    }

    @Override
    public VehicleBasicInfo getBasicInfo(Long vehicleId) {
        return vehicleBasicService.getBasicInfo(vehicleId);
    }

    @Override
    public List<VehicleBasicInfo> listCandidates(VehicleCandidateRequest request) {
        return vehicleBasicService.getCandidates(request.getStage(), request.getVinPattern());
    }

    @Override
    public boolean checkVinAvailable(String vin) {
        return !vehicleBasicService.existsByVin(vin == null ? null : vin.trim().toUpperCase());
    }
}

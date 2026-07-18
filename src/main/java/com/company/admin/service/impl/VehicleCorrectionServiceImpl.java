package com.company.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.entity.Vehicle;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.VehicleCorrectionService;
import com.company.admin.service.VehiclePanoramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleCorrectionServiceImpl implements VehicleCorrectionService {

    private final VehicleMapper vehicleMapper;
    private final VehiclePanoramaService vehiclePanoramaService;
    private final BusinessStatusLabelService statusLabelService;

    @Override
    public PageResult<VehicleListResponse> pageCorrections(VehicleQueryRequest request) {
        Page<VehicleListResponse> page = vehicleMapper.selectVehicleCorrectionPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        page.getRecords().forEach(this::applyLabels);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    public VehiclePanoramaResponse getCorrection(Long vehicleId) {
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null || Integer.valueOf(1).equals(vehicle.getDeleted())) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        return vehiclePanoramaService.getPanorama(vehicle.getVin());
    }

    private void applyLabels(VehicleListResponse response) {
        response.setLifecycleStageLabel(statusLabelService.lifecycleStageLabel(response.getLifecycleStage()));
        response.setProductionStatusLabel(statusLabelService.stageStatusLabel(response.getProductionStatus()));
    }
}

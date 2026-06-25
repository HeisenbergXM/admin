package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.Vehicle;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.service.VehicleBasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleBasicServiceImpl implements VehicleBasicService {

    private final VehicleMapper vehicleMapper;

    @Override
    public Vehicle getById(Long vehicleId) {
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        return vehicle;
    }

    @Override
    public VehicleBasicInfo getBasicInfo(Long vehicleId) {
        VehicleBasicInfo info = vehicleMapper.selectBasicInfoById(vehicleId);
        if (info == null) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        return info;
    }

    @Override
    public List<VehicleBasicInfo> getBasicInfoByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return vehicleMapper.selectBasicInfoByIds(ids);
    }

    @Override
    public boolean existsByVin(String vin) {
        return vehicleMapper.selectCount(
                new LambdaQueryWrapper<Vehicle>()
                        .eq(Vehicle::getVin, vin)
                        .eq(Vehicle::getDeleted, 0)
        ) > 0;
    }

    @Override
    public List<VehicleBasicInfo> getCandidates(String stage, String vinPattern) {
        return vehicleMapper.selectCandidates(stage, vinPattern);
    }
}

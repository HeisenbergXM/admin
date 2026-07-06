package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.AllocationQueryRequest;
import com.company.admin.dto.request.AllocationSaveRequest;
import com.company.admin.dto.response.AllocationResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.VehAllocation;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehAllocationMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehicleBasicService;
import com.company.admin.service.VehAllocationService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehAllocationServiceImpl implements VehAllocationService {

    private final VehAllocationMapper vehAllocationMapper;
    private final LifecycleService lifecycleService;
    private final VehicleBasicService vehicleBasicService;

    @Override
    public PageResult<AllocationResponse> pageAllocations(AllocationQueryRequest request) {
        Page<AllocationResponse> page = vehAllocationMapper.selectAllocationPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public Long createAllocation(Long vehicleId, AllocationSaveRequest request) {
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_ALLOCATION);
        if (findAllocation(vehicleId) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆已存在销售分配记录");
        }
        VehAllocation allocation = new VehAllocation();
        copyFields(request, allocation);
        allocation.setVehicleId(vehicleId);
        allocation.setStageStatus(StageStatus.DRAFT.name());
        vehAllocationMapper.insert(allocation);
        return allocation.getId();
    }

    @Override
    @Transactional
    public void updateAllocation(Long vehicleId, AllocationSaveRequest request) {
        VehAllocation allocation = getAllocationEntity(vehicleId);
        lifecycleService.assertNotConfirmed(allocation.getStageStatus());
        copyFields(request, allocation);
        vehAllocationMapper.updateById(allocation);
    }

    @Override
    @Transactional
    public void confirmAllocation(Long vehicleId) {
        VehAllocation allocation = getAllocationEntity(vehicleId);
        lifecycleService.confirmAndAdvance(
                vehicleId,
                allocation.getStageStatus(),
                LifecycleStage.PENDING_ALLOCATION,
                LifecycleStage.PENDING_INVOICE,
                () -> {
                    allocation.setStageStatus(StageStatus.CONFIRMED.name());
                    allocation.setConfirmedBy(SecurityUtils.getCurrentUsername());
                    allocation.setConfirmedAt(LocalDateTime.now());
                    vehAllocationMapper.updateById(allocation);
                });
    }

    @Override
    public AllocationResponse getAllocation(Long vehicleId) {
        VehAllocation allocation = findAllocation(vehicleId);
        if (allocation != null) {
            return toResponse(allocation);
        }

        VehicleBasicInfo basicInfo = vehicleBasicService.getBasicInfo(vehicleId);
        if (!LifecycleStage.PENDING_ALLOCATION.name().equals(basicInfo.getLifecycleStage())) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }

        AllocationResponse response = new AllocationResponse();
        response.setVehicleId(vehicleId);
        response.setStageStatus(basicInfo.getLifecycleStage());
        copyBasicInfo(basicInfo, response);
        return response;
    }

    private VehAllocation findAllocation(Long vehicleId) {
        return vehAllocationMapper.selectOne(new LambdaQueryWrapper<VehAllocation>()
                .eq(VehAllocation::getVehicleId, vehicleId)
                .eq(VehAllocation::getDeleted, 0));
    }

    private VehAllocation getAllocationEntity(Long vehicleId) {
        VehAllocation allocation = findAllocation(vehicleId);
        if (allocation == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return allocation;
    }

    private void copyFields(AllocationSaveRequest request, VehAllocation allocation) {
        allocation.setAllocatedDate(request.getAllocatedDate());
        allocation.setDealerId(request.getDealerId());
        allocation.setSalesStatus(request.getSalesStatus());
        allocation.setRemark3(request.getRemark3());
    }

    private AllocationResponse toResponse(VehAllocation allocation) {
        AllocationResponse response = new AllocationResponse();
        BeanUtils.copyProperties(allocation, response);
        if (vehicleBasicService != null) {
            VehicleBasicInfo basicInfo = vehicleBasicService.getBasicInfo(allocation.getVehicleId());
            copyBasicInfo(basicInfo, response);
        }
        return response;
    }

    private void copyBasicInfo(VehicleBasicInfo basicInfo, AllocationResponse response) {
        response.setVin(basicInfo.getVin());
        response.setLifecycleStage(basicInfo.getLifecycleStage());
        response.setModelId(basicInfo.getModelId());
        response.setModelName(basicInfo.getModelName());
        response.setSeries(basicInfo.getSeries());
        response.setSpec(basicInfo.getSpec());
        response.setModelCode(basicInfo.getModelCode());
        response.setYearMake(basicInfo.getYearMake());
        response.setExteriorColorId(basicInfo.getExteriorColorId());
        response.setExteriorColorName(basicInfo.getExteriorColorName());
        response.setInteriorColorId(basicInfo.getInteriorColorId());
        response.setInteriorColorName(basicInfo.getInteriorColorName());
        response.setDealerName(basicInfo.getDealerName());
    }
}

package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.ProductionSaveRequest;
import com.company.admin.dto.response.ProductionResponse;
import com.company.admin.entity.Vehicle;
import com.company.admin.entity.VehProduction;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.mapper.VehProductionMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehProductionService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehProductionServiceImpl implements VehProductionService {

    private final VehicleMapper vehicleMapper;
    private final VehProductionMapper vehProductionMapper;
    private final LifecycleService lifecycleService;

    @Override
    @Transactional
    public Long createProduction(ProductionSaveRequest request) {
        if (existsByVin(request.getVin())) {
            throw new BusinessException(ErrorCode.VIN_DUPLICATE);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setVin(normalizeVin(request.getVin()));
        vehicle.setLifecycleStage(LifecycleStage.PENDING_OFFLINE.name());
        vehicleMapper.insert(vehicle);

        VehProduction production = new VehProduction();
        copySaveFields(request, production);
        production.setVehicleId(vehicle.getId());
        production.setStageStatus(StageStatus.DRAFT.name());
        vehProductionMapper.insert(production);
        return vehicle.getId();
    }

    @Override
    @Transactional
    public void updateProduction(Long vehicleId, ProductionSaveRequest request) {
        VehProduction existing = getProductionEntity(vehicleId);
        lifecycleService.assertNotConfirmed(existing.getStageStatus());

        VehProduction update = new VehProduction();
        copySaveFields(request, update);
        update.setId(existing.getId());
        update.setVehicleId(vehicleId);
        vehProductionMapper.updateById(update);
    }

    @Override
    @Transactional
    public void confirmProduction(Long vehicleId) {
        VehProduction production = getProductionEntity(vehicleId);
        lifecycleService.confirmAndAdvance(
                vehicleId,
                production.getStageStatus(),
                LifecycleStage.PENDING_OFFLINE,
                LifecycleStage.PENDING_INBOUND,
                () -> {
                    production.setStageStatus(StageStatus.CONFIRMED.name());
                    production.setConfirmedBy(SecurityUtils.getCurrentUsername());
                    production.setConfirmedAt(LocalDateTime.now());
                    vehProductionMapper.updateById(production);
                });
    }

    @Override
    public ProductionResponse getProduction(Long vehicleId) {
        return toResponse(getProductionEntity(vehicleId));
    }

    private boolean existsByVin(String vin) {
        return vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getVin, normalizeVin(vin))
                .eq(Vehicle::getDeleted, 0)) > 0;
    }

    private VehProduction getProductionEntity(Long vehicleId) {
        VehProduction production = vehProductionMapper.selectOne(new LambdaQueryWrapper<VehProduction>()
                .eq(VehProduction::getVehicleId, vehicleId)
                .eq(VehProduction::getDeleted, 0));
        if (production == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return production;
    }

    private void copySaveFields(ProductionSaveRequest request, VehProduction production) {
        production.setModelId(request.getModelId());
        production.setExteriorColorId(request.getExteriorColorId());
        production.setInteriorColorId(request.getInteriorColorId());
        production.setEngineNumber(request.getEngineNumber());
        production.setYearMake(request.getYearMake());
        production.setMaterial(request.getMaterial());
        production.setShipment(request.getShipment());
        production.setBatch(request.getBatch());
        production.setOfflineEpmbDate(request.getOfflineEpmbDate());
        production.setEpmbOkDate(request.getEpmbOkDate());
        production.setRemark1(request.getRemark1());
    }

    private ProductionResponse toResponse(VehProduction production) {
        ProductionResponse response = new ProductionResponse();
        BeanUtils.copyProperties(production, response);
        return response;
    }

    private String normalizeVin(String vin) {
        return vin == null ? null : vin.trim().toUpperCase();
    }
}

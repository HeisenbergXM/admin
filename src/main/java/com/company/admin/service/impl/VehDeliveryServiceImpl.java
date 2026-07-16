package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DeliveryQueryRequest;
import com.company.admin.dto.request.DeliverySaveRequest;
import com.company.admin.dto.response.DeliveryResponse;
import com.company.admin.entity.VehAllocation;
import com.company.admin.entity.VehDelivery;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehAllocationMapper;
import com.company.admin.mapper.VehDeliveryMapper;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehDeliveryService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehDeliveryServiceImpl implements VehDeliveryService {

    private final VehDeliveryMapper vehDeliveryMapper;
    private final VehAllocationMapper vehAllocationMapper;
    private final LifecycleService lifecycleService;
    private final BusinessStatusLabelService statusLabelService;

    @Override
    public PageResult<DeliveryResponse> pageDeliveries(DeliveryQueryRequest request) {
        Page<DeliveryResponse> page = vehDeliveryMapper.selectDeliveryPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        page.getRecords().forEach(this::applyLabels);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public Long createDelivery(Long vehicleId, DeliverySaveRequest request) {
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_DELIVERY);
        validateDraft(request);
        if (findDelivery(vehicleId) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆已存在配送记录");
        }
        VehDelivery delivery = new VehDelivery();
        copyFields(request, delivery);
        delivery.setVehicleId(vehicleId);
        delivery.setStageStatus(StageStatus.DRAFT.name());
        vehDeliveryMapper.insert(delivery);
        return delivery.getId();
    }

    @Override
    @Transactional
    public void updateDelivery(Long vehicleId, DeliverySaveRequest request) {
        VehDelivery delivery = getDeliveryEntity(vehicleId);
        lifecycleService.assertNotConfirmed(delivery.getStageStatus());
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_DELIVERY);
        validateDraft(request);
        copyFields(request, delivery);
        vehDeliveryMapper.updateById(delivery);
    }

    @Override
    @Transactional
    public void confirmDelivery(Long vehicleId) {
        VehDelivery delivery = getDeliveryEntity(vehicleId);
        lifecycleService.assertNotConfirmed(delivery.getStageStatus());
        if (delivery.getReceivedDate() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "经销商签收日期不能为空");
        }
        VehAllocation allocation = vehAllocationMapper.selectOne(
                new LambdaQueryWrapper<VehAllocation>()
                        .eq(VehAllocation::getVehicleId, vehicleId)
                        .eq(VehAllocation::getStageStatus, StageStatus.CONFIRMED.name())
                        .eq(VehAllocation::getDeleted, 0));
        if (allocation == null || allocation.getDealerId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆未完成经销商分配");
        }
        lifecycleService.confirmAndAdvance(
                vehicleId,
                delivery.getStageStatus(),
                LifecycleStage.PENDING_DELIVERY,
                LifecycleStage.PENDING_REGISTRATION,
                () -> {
                    delivery.setStageStatus(StageStatus.CONFIRMED.name());
                    delivery.setConfirmedBy(SecurityUtils.getCurrentUsername());
                    delivery.setConfirmedAt(LocalDateTime.now());
                    vehDeliveryMapper.updateById(delivery);
                });
    }

    @Override
    public DeliveryResponse getDelivery(Long vehicleId) {
        DeliveryResponse response = vehDeliveryMapper.selectDeliveryByVehicleId(vehicleId);
        if (response == null || response.getId() == null
                && !LifecycleStage.PENDING_DELIVERY.name().equals(response.getLifecycleStage())) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        applyLabels(response);
        return response;
    }

    private VehDelivery findDelivery(Long vehicleId) {
        return vehDeliveryMapper.selectOne(new LambdaQueryWrapper<VehDelivery>()
                .eq(VehDelivery::getVehicleId, vehicleId)
                .eq(VehDelivery::getDeleted, 0));
    }

    private VehDelivery getDeliveryEntity(Long vehicleId) {
        VehDelivery delivery = findDelivery(vehicleId);
        if (delivery == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return delivery;
    }

    private void validateDraft(DeliverySaveRequest request) {
        if (request.getEtdToDealer() != null && request.getEtaToDealer() != null
                && request.getEtaToDealer().isBefore(request.getEtdToDealer())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "预计到达日期不能早于发车日期");
        }
        if (request.getTrollyType() != null
                && !"4 units".equals(request.getTrollyType())
                && !"6 units".equals(request.getTrollyType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "轿运车类型只能为4 units或6 units");
        }
    }

    private void copyFields(DeliverySaveRequest request, VehDelivery delivery) {
        delivery.setEtdToDealer(request.getEtdToDealer());
        delivery.setEtaToDealer(request.getEtaToDealer());
        delivery.setTrollyType(request.getTrollyType());
        delivery.setFullyLoad(request.getFullyLoad());
        delivery.setReceivedDate(request.getReceivedDate());
        delivery.setDeliveryStatus(request.getDeliveryStatus());
        delivery.setRemark7(request.getRemark7());
    }

    private void applyLabels(DeliveryResponse response) {
        response.setLifecycleStageLabel(statusLabelService.lifecycleStageLabel(response.getLifecycleStage()));
        response.setStageStatusLabel(statusLabelService.stageStatusLabel(response.getStageStatus()));
        response.setDeliveryStatusLabel(statusLabelService.dictLabel("delivery_status", response.getDeliveryStatus()));
    }
}

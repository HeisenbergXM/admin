package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.InboundQueryRequest;
import com.company.admin.dto.request.InboundSaveRequest;
import com.company.admin.dto.response.InboundResponse;
import com.company.admin.entity.VehInbound;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehInboundMapper;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehInboundService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehInboundServiceImpl implements VehInboundService {

    private final VehInboundMapper vehInboundMapper;
    private final LifecycleService lifecycleService;
    private final BusinessStatusLabelService statusLabelService;

    @Override
    public PageResult<InboundResponse> pageInbounds(InboundQueryRequest request) {
        Page<InboundResponse> page = vehInboundMapper.selectInboundPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        page.getRecords().forEach(this::applyLabels);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public Long createInbound(Long vehicleId, InboundSaveRequest request) {
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_INBOUND);
        if (findInbound(vehicleId) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆已存在入库记录");
        }
        VehInbound inbound = new VehInbound();
        copyFields(request, inbound);
        inbound.setVehicleId(vehicleId);
        inbound.setStageStatus(StageStatus.DRAFT.name());
        vehInboundMapper.insert(inbound);
        return inbound.getId();
    }

    @Override
    @Transactional
    public void updateInbound(Long vehicleId, InboundSaveRequest request) {
        VehInbound inbound = getInboundEntity(vehicleId);
        lifecycleService.assertNotConfirmed(inbound.getStageStatus());
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_INBOUND);
        copyFields(request, inbound);
        vehInboundMapper.updateById(inbound);
    }

    @Override
    @Transactional
    public void confirmInbound(Long vehicleId) {
        VehInbound inbound = getInboundEntity(vehicleId);
        lifecycleService.assertNotConfirmed(inbound.getStageStatus());
        if (inbound.getSaicBuyOffDate() == null || inbound.getDateToStorageYard() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                    "SAIC buy off 日期和到中转仓库日期不能为空");
        }
        lifecycleService.confirmAndAdvance(
                vehicleId,
                inbound.getStageStatus(),
                LifecycleStage.PENDING_INBOUND,
                LifecycleStage.PENDING_ALLOCATION,
                () -> {
                    inbound.setStageStatus(StageStatus.CONFIRMED.name());
                    inbound.setConfirmedBy(SecurityUtils.getCurrentUsername());
                    inbound.setConfirmedAt(LocalDateTime.now());
                    vehInboundMapper.updateById(inbound);
                });
    }

    @Override
    public InboundResponse getInbound(Long vehicleId) {
        InboundResponse response = vehInboundMapper.selectInboundByVehicleId(vehicleId);
        if (response == null || response.getId() == null
                && !LifecycleStage.PENDING_INBOUND.name().equals(response.getLifecycleStage())) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        applyLabels(response);
        return response;
    }

    private VehInbound findInbound(Long vehicleId) {
        return vehInboundMapper.selectOne(new LambdaQueryWrapper<VehInbound>()
                .eq(VehInbound::getVehicleId, vehicleId)
                .eq(VehInbound::getDeleted, 0));
    }

    private VehInbound getInboundEntity(Long vehicleId) {
        VehInbound inbound = findInbound(vehicleId);
        if (inbound == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return inbound;
    }

    private void copyFields(InboundSaveRequest request, VehInbound inbound) {
        inbound.setSaicBuyOffDate(request.getSaicBuyOffDate());
        inbound.setDateToStorageYard(request.getDateToStorageYard());
        inbound.setRemark2(request.getRemark2());
    }

    private void applyLabels(InboundResponse response) {
        response.setLifecycleStageLabel(statusLabelService.lifecycleStageLabel(response.getLifecycleStage()));
        response.setStageStatusLabel(statusLabelService.stageStatusLabel(response.getStageStatus()));
    }
}

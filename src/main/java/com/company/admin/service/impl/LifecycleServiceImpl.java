package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.entity.Vehicle;
import com.company.admin.entity.VehInvoice;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehInvoiceMapper;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LifecycleServiceImpl implements LifecycleService {

    private final VehicleMapper vehicleMapper;
    private final VehInvoiceMapper vehInvoiceMapper;

    @Override
    public LifecycleStage getCurrentStage(Long vehicleId) {
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        return LifecycleStage.valueOf(vehicle.getLifecycleStage());
    }

    @Override
    public void assertStage(Long vehicleId, LifecycleStage expected) {
        LifecycleStage current = getCurrentStage(vehicleId);
        if (current != expected) {
            throw new BusinessException(
                    ErrorCode.LIFECYCLE_STAGE_MISMATCH.getCode(),
                    String.format("车辆当前阶段为[%s]，需要[%s]",
                            current.getLabel(), expected.getLabel()));
        }
    }

    @Override
    @Transactional
    public void advanceStage(Long vehicleId, LifecycleStage from, LifecycleStage to) {
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        LifecycleStage current = LifecycleStage.valueOf(vehicle.getLifecycleStage());
        if (current != from) {
            throw new BusinessException(
                    ErrorCode.LIFECYCLE_STAGE_MISMATCH.getCode(),
                    String.format("阶段推进失败：当前[%s]，预期[%s]→[%s]",
                            current.getLabel(), from.getLabel(), to.getLabel()));
        }
        vehicle.setLifecycleStage(to.name());
        vehicle.setUpdatedBy(SecurityUtils.getCurrentUsername());
        vehicleMapper.updateById(vehicle);
    }

    @Override
    public void assertNotConfirmed(String stageStatus) {
        if (StageStatus.CONFIRMED.name().equals(stageStatus)) {
            throw new BusinessException(ErrorCode.STAGE_ALREADY_CONFIRMED);
        }
    }

    @Override
    @Transactional
    public void confirmAndAdvance(Long vehicleId, String stageStatus,
                                  LifecycleStage from, LifecycleStage to,
                                  Runnable lockAction) {
        assertNotConfirmed(stageStatus);
        // 执行锁定：设置 confirmed_by/at + stage_status=CONFIRMED + 更新 DB
        lockAction.run();
        // 推进主表生命周期
        advanceStage(vehicleId, from, to);
    }

    @Override
    public boolean hasValidFormalInvoice(Long vehicleId) {
        List<VehInvoice> invoices = vehInvoiceMapper.selectList(
                new LambdaQueryWrapper<VehInvoice>()
                        .eq(VehInvoice::getVehicleId, vehicleId)
                        .eq(VehInvoice::getStageStatus, StageStatus.CONFIRMED.name())
                        .eq(VehInvoice::getDeleted, 0)
                        .orderByDesc(VehInvoice::getInvoiceSeq));
        if (invoices.isEmpty()) {
            return false;
        }
        // 有效发票 = seq 最大且为正式发票
        VehInvoice latest = invoices.get(0);
        return "INVOICED".equals(latest.getInvoiceType());
    }
}

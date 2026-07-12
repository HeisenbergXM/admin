package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.PaymentQueryRequest;
import com.company.admin.dto.request.PaymentSaveRequest;
import com.company.admin.dto.response.PaymentResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.VehPayment;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehPaymentMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.VehicleBasicService;
import com.company.admin.service.VehPaymentService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehPaymentServiceImpl implements VehPaymentService {

    private final VehPaymentMapper vehPaymentMapper;
    private final LifecycleService lifecycleService;
    private final VehicleBasicService vehicleBasicService;
    private final BusinessStatusLabelService statusLabelService;

    @Override
    public PageResult<PaymentResponse> pagePayments(PaymentQueryRequest request) {
        Page<PaymentResponse> page = vehPaymentMapper.selectPaymentPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        page.getRecords().forEach(this::applyLabels);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public Long createPayment(Long vehicleId, PaymentSaveRequest request) {
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_PAYMENT);
        if (findPayment(vehicleId) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆已存在收款记录");
        }
        VehPayment payment = new VehPayment();
        copyFields(request, payment);
        payment.setVehicleId(vehicleId);
        payment.setStageStatus(StageStatus.DRAFT.name());
        vehPaymentMapper.insert(payment);
        return payment.getId();
    }

    @Override
    @Transactional
    public void updatePayment(Long vehicleId, PaymentSaveRequest request) {
        VehPayment payment = getPaymentEntity(vehicleId);
        lifecycleService.assertNotConfirmed(payment.getStageStatus());
        copyFields(request, payment);
        vehPaymentMapper.updateById(payment);
    }

    @Override
    @Transactional
    public void confirmPayment(Long vehicleId) {
        VehPayment payment = getPaymentEntity(vehicleId);
        if (!lifecycleService.hasValidFormalInvoice(vehicleId)) {
            throw new BusinessException(ErrorCode.INVOICE_NOT_FORMAL);
        }
        lifecycleService.confirmAndAdvance(
                vehicleId,
                payment.getStageStatus(),
                LifecycleStage.PENDING_PAYMENT,
                LifecycleStage.PENDING_DELIVERY,
                () -> {
                    payment.setStageStatus(StageStatus.CONFIRMED.name());
                    payment.setConfirmedBy(SecurityUtils.getCurrentUsername());
                    payment.setConfirmedAt(LocalDateTime.now());
                    vehPaymentMapper.updateById(payment);
                });
    }

    @Override
    public PaymentResponse getPayment(Long vehicleId) {
        return toResponse(getPaymentEntity(vehicleId));
    }

    private VehPayment findPayment(Long vehicleId) {
        return vehPaymentMapper.selectOne(new LambdaQueryWrapper<VehPayment>()
                .eq(VehPayment::getVehicleId, vehicleId)
                .eq(VehPayment::getDeleted, 0));
    }

    private VehPayment getPaymentEntity(Long vehicleId) {
        VehPayment payment = findPayment(vehicleId);
        if (payment == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return payment;
    }

    private void copyFields(PaymentSaveRequest request, VehPayment payment) {
        payment.setPaymentDate(request.getPaymentDate());
        payment.setCreditFullPaymentDate(request.getCreditFullPaymentDate());
        payment.setPaymentStatus(request.getPaymentStatus());
        payment.setRemark5(request.getRemark5());
    }

    private PaymentResponse toResponse(VehPayment payment) {
        PaymentResponse response = new PaymentResponse();
        BeanUtils.copyProperties(payment, response);
        response.setHasFormalInvoice(lifecycleService.hasValidFormalInvoice(payment.getVehicleId()));
        if (vehicleBasicService != null) {
            VehicleBasicInfo basicInfo = vehicleBasicService.getBasicInfo(payment.getVehicleId());
            response.setVin(basicInfo.getVin());
            response.setLifecycleStage(basicInfo.getLifecycleStage());
        }
        applyLabels(response);
        return response;
    }

    private void applyLabels(PaymentResponse response) {
        response.setLifecycleStageLabel(statusLabelService.lifecycleStageLabel(response.getLifecycleStage()));
        response.setStageStatusLabel(statusLabelService.stageStatusLabel(response.getStageStatus()));
        response.setPaymentStatusLabel(statusLabelService.dictLabel("payment_status", response.getPaymentStatus()));
    }
}

package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.RegistrationQueryRequest;
import com.company.admin.dto.request.RegistrationSaveRequest;
import com.company.admin.dto.response.RegistrationResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.VehRegistration;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehRegistrationMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehicleBasicService;
import com.company.admin.service.VehRegistrationService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehRegistrationServiceImpl implements VehRegistrationService {

    private final VehRegistrationMapper vehRegistrationMapper;
    private final LifecycleService lifecycleService;
    private final VehicleBasicService vehicleBasicService;

    @Override
    public PageResult<RegistrationResponse> pageRegistrations(RegistrationQueryRequest request) {
        Page<RegistrationResponse> page = vehRegistrationMapper.selectRegistrationPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public Long createRegistration(Long vehicleId, RegistrationSaveRequest request) {
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_REGISTRATION);
        if (findRegistration(vehicleId) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆已存在上牌记录");
        }
        VehRegistration registration = new VehRegistration();
        copyFields(request, registration);
        registration.setVehicleId(vehicleId);
        registration.setStageStatus(StageStatus.DRAFT.name());
        vehRegistrationMapper.insert(registration);
        return registration.getId();
    }

    @Override
    @Transactional
    public void updateRegistration(Long vehicleId, RegistrationSaveRequest request) {
        VehRegistration registration = getRegistrationEntity(vehicleId);
        lifecycleService.assertNotConfirmed(registration.getStageStatus());
        copyFields(request, registration);
        vehRegistrationMapper.updateById(registration);
    }

    @Override
    @Transactional
    public void confirmRegistration(Long vehicleId) {
        VehRegistration registration = getRegistrationEntity(vehicleId);
        lifecycleService.confirmAndAdvance(
                vehicleId,
                registration.getStageStatus(),
                LifecycleStage.PENDING_REGISTRATION,
                LifecycleStage.COMPLETED,
                () -> {
                    registration.setStageStatus(StageStatus.CONFIRMED.name());
                    registration.setConfirmedBy(SecurityUtils.getCurrentUsername());
                    registration.setConfirmedAt(LocalDateTime.now());
                    vehRegistrationMapper.updateById(registration);
                });
    }

    @Override
    public RegistrationResponse getRegistration(Long vehicleId) {
        return toResponse(getRegistrationEntity(vehicleId));
    }

    private VehRegistration findRegistration(Long vehicleId) {
        return vehRegistrationMapper.selectOne(new LambdaQueryWrapper<VehRegistration>()
                .eq(VehRegistration::getVehicleId, vehicleId)
                .eq(VehRegistration::getDeleted, 0));
    }

    private VehRegistration getRegistrationEntity(Long vehicleId) {
        VehRegistration registration = findRegistration(vehicleId);
        if (registration == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return registration;
    }

    private void copyFields(RegistrationSaveRequest request, VehRegistration registration) {
        registration.setDrosstechStatus(request.getDrosstechStatus());
        registration.setUploadDate(request.getUploadDate());
        registration.setRegistrationDate(request.getRegistrationDate());
        registration.setCustomerRegion(request.getCustomerRegion());
        registration.setRemark8(request.getRemark8());
    }

    private RegistrationResponse toResponse(VehRegistration registration) {
        RegistrationResponse response = new RegistrationResponse();
        BeanUtils.copyProperties(registration, response);
        if (vehicleBasicService != null) {
            VehicleBasicInfo basicInfo = vehicleBasicService.getBasicInfo(registration.getVehicleId());
            response.setVin(basicInfo.getVin());
            response.setDealerId(basicInfo.getDealerId());
            response.setDealerName(basicInfo.getDealerName());
        }
        return response;
    }
}

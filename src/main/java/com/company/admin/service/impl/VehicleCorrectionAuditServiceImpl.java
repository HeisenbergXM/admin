package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.entity.OperationLog;
import com.company.admin.mapper.OperationLogMapper;
import com.company.admin.service.VehicleCorrectionAuditService;
import com.company.admin.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehicleCorrectionAuditServiceImpl implements VehicleCorrectionAuditService {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void recordSuccess(CorrectionDiff diff) {
        OperationLog log = new OperationLog();
        log.setUsername(SecurityUtils.getCurrentUsername());
        log.setOperation("车辆数据修订-字段差异");
        log.setMethod("VehicleCorrectionService.updateCorrection");
        log.setCreateTime(LocalDateTime.now());
        try {
            log.setParams(objectMapper.writeValueAsString(diff));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.setResult("SUCCESS");
        operationLogMapper.insert(log);
    }
}

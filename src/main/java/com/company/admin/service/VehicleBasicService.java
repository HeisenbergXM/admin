package com.company.admin.service;

import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.Vehicle;

import java.util.List;

/**
 * 车辆主表基础查询服务（跨阶段复用）
 */
public interface VehicleBasicService {

    /**
     * 按 ID 获取车辆实体
     */
    Vehicle getById(Long vehicleId);

    /**
     * 按 ID 获取车辆基本信息（含车型/颜色/经销商名称）
     */
    VehicleBasicInfo getBasicInfo(Long vehicleId);

    /**
     * 批量获取车辆基本信息
     */
    List<VehicleBasicInfo> getBasicInfoByIds(List<Long> ids);

    /**
     * VIN 唯一性校验，已存在返回 true
     */
    boolean existsByVin(String vin);

    /**
     * 按阶段查询候选车辆（VIN 候选选择器用）
     */
    List<VehicleBasicInfo> getCandidates(String stage, String vinPattern);
}

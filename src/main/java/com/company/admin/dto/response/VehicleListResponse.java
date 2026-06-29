package com.company.admin.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆列表响应。
 */
@Data
public class VehicleListResponse {

    private Long id;
    private String vin;
    private String lifecycleStage;
    private Long modelId;
    private String modelName;
    private String series;
    private String spec;
    private String modelCode;
    private String yearMake;
    private Long exteriorColorId;
    private String exteriorColorName;
    private Long interiorColorId;
    private String interiorColorName;
    private Long dealerId;
    private String dealerName;
    private String productionStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

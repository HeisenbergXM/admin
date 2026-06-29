package com.company.admin.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产阶段响应。
 */
@Data
public class ProductionResponse {

    private Long id;
    private Long vehicleId;
    private String stageStatus;
    private Long modelId;
    private Long exteriorColorId;
    private Long interiorColorId;
    private String engineNumber;
    private String yearMake;
    private String material;
    private String shipment;
    private String batch;
    private LocalDate offlineEpmbDate;
    private LocalDate epmbOkDate;
    private String remark1;
    private String confirmedBy;
    private LocalDateTime confirmedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}

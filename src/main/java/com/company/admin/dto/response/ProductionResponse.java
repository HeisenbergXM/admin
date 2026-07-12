package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产阶段响应。
 */
@Data
@Schema(description = "生产阶段响应")
public class ProductionResponse {

    @Schema(description = "主键 ID")
    private Long id;
    @Schema(description = "车辆 ID")
    private Long vehicleId;
    @Schema(description = "阶段记录状态：DRAFT=草稿，CONFIRMED=已确认")
    private String stageStatus;
    @Schema(description = "阶段记录状态中文名称")
    private String stageStatusLabel;
    @Schema(description = "车型 ID")
    private Long modelId;
    @Schema(description = "外饰颜色 ID")
    private Long exteriorColorId;
    @Schema(description = "内饰颜色 ID")
    private Long interiorColorId;
    @Schema(description = "发动机号")
    private String engineNumber;
    @Schema(description = "年款")
    private String yearMake;
    @Schema(description = "物料信息")
    private String material;
    @Schema(description = "Shipment 信息")
    private String shipment;
    @Schema(description = "批次")
    private String batch;
    @Schema(description = "Offline EPMB 日期")
    private LocalDate offlineEpmbDate;
    @Schema(description = "EPMB OK 日期")
    private LocalDate epmbOkDate;
    @Schema(description = "生产阶段备注")
    private String remark1;
    @Schema(description = "确认人")
    private String confirmedBy;
    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;
    @Schema(description = "创建人")
    private String createdBy;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新人")
    private String updatedBy;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

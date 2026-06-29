package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆列表响应。
 */
@Data
@Schema(description = "车辆列表项")
public class VehicleListResponse {

    @Schema(description = "主键 ID")
    private Long id;
    @Schema(description = "车辆识别码 VIN", example = "LSJW56U95RG000001")
    private String vin;
    @Schema(description = "当前生命周期阶段")
    private String lifecycleStage;
    @Schema(description = "车型 ID")
    private Long modelId;
    @Schema(description = "车型名称")
    private String modelName;
    @Schema(description = "车系")
    private String series;
    @Schema(description = "配置规格")
    private String spec;
    @Schema(description = "车型代码")
    private String modelCode;
    @Schema(description = "年款")
    private String yearMake;
    @Schema(description = "外饰颜色 ID")
    private Long exteriorColorId;
    @Schema(description = "外饰颜色名称")
    private String exteriorColorName;
    @Schema(description = "内饰颜色 ID")
    private Long interiorColorId;
    @Schema(description = "内饰颜色名称")
    private String interiorColorName;
    @Schema(description = "经销商 ID")
    private Long dealerId;
    @Schema(description = "经销商名称")
    private String dealerName;
    @Schema(description = "生产阶段记录状态")
    private String productionStatus;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

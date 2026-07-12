package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 车辆基本信息 DTO（只读，列表/选择器/详情页复用）
 * 实时取自 t_vehicle 主表 + 主数据表，不冗余存储
 */
@Data
@Schema(description = "车辆基本信息")
public class VehicleBasicInfo {

    @Schema(description = "主键 ID")
    private Long id;
    @Schema(description = "车辆识别码 VIN", example = "LSJW56U95RG000001")
    private String vin;
    @Schema(description = "当前生命周期阶段")
    private String lifecycleStage;
    @Schema(description = "当前生命周期阶段中文名称")
    private String lifecycleStageLabel;

    // 来自 t_md_model
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

    // 来自 t_veh_production
    @Schema(description = "年款")
    private String yearMake;

    // 来自 t_md_exterior_color
    @Schema(description = "外饰颜色 ID")
    private Long exteriorColorId;
    @Schema(description = "外饰颜色名称")
    private String exteriorColorName;

    // 来自 t_md_interior_color
    @Schema(description = "内饰颜色 ID")
    private Long interiorColorId;
    @Schema(description = "内饰颜色名称")
    private String interiorColorName;

    // 来自 t_veh_allocation（经销商信息，部分场景需要）
    @Schema(description = "经销商 ID")
    private Long dealerId;
    @Schema(description = "经销商名称")
    private String dealerName;
}

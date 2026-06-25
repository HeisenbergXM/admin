package com.company.admin.dto.response;

import lombok.Data;

/**
 * 车辆基本信息 DTO（只读，列表/选择器/详情页复用）
 * 实时取自 t_vehicle 主表 + 主数据表，不冗余存储
 */
@Data
public class VehicleBasicInfo {

    private Long id;
    private String vin;
    private String lifecycleStage;

    // 来自 t_md_model
    private Long modelId;
    private String modelName;
    private String series;
    private String spec;
    private String modelCode;

    // 来自 t_veh_production
    private String yearMake;

    // 来自 t_md_exterior_color
    private Long exteriorColorId;
    private String exteriorColorName;

    // 来自 t_md_interior_color
    private Long interiorColorId;
    private String interiorColorName;

    // 来自 t_veh_allocation（经销商信息，部分场景需要）
    private Long dealerId;
    private String dealerName;
}

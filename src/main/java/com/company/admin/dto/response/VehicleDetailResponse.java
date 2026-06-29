package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 车辆详情响应。
 */
@Data
@Schema(description = "车辆详情响应")
public class VehicleDetailResponse {

    @Schema(description = "车辆基本信息")
    private VehicleBasicInfo basicInfo;
    @Schema(description = "生产阶段数据")
    private ProductionResponse production;
}

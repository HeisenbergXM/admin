package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆通用分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "车辆分页查询请求")
public class VehicleQueryRequest extends PageRequest {

    @Schema(description = "车辆识别码 VIN", example = "LSJW56U95RG000001")
    private String vin;
    @Schema(description = "车型 ID")
    private Long modelId;
    @Schema(description = "车型名称")
    private String modelName;
    @Schema(description = "车系")
    private String series;
    @Schema(description = "当前生命周期阶段")
    private String lifecycleStage;
    @Schema(description = "经销商 ID")
    private Long dealerId;
}

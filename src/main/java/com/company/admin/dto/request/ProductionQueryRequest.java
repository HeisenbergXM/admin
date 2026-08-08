package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生产录入待办分页查询请求。
 * 固定生命周期阶段 PENDING_OFFLINE 由后端接口保证，不暴露给调用方。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "生产录入待办分页查询请求")
public class ProductionQueryRequest extends PageRequest {

    @Schema(description = "车辆识别码 VIN", example = "LSJW56U95RG000001")
    private String vin;
    @Schema(description = "车型 ID")
    private Long modelId;
    @Schema(description = "车型名称")
    private String modelName;
    @Schema(description = "车系")
    private String series;
}

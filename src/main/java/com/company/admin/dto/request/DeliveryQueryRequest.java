package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "单车配送查询请求")
public class DeliveryQueryRequest extends PageRequest {

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "车型 ID")
    private Long modelId;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "阶段状态：PENDING_DELIVERY 按车辆生命周期过滤，DRAFT/CONFIRMED 按配送记录状态过滤")
    private String stageStatus;
}

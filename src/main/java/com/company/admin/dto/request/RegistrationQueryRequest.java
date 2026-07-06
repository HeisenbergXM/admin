package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "上牌查询请求")
public class RegistrationQueryRequest extends PageRequest {

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "阶段状态：PENDING_REGISTRATION 按车辆生命周期过滤，DRAFT/CONFIRMED 按上牌记录状态过滤")
    private String stageStatus;
}

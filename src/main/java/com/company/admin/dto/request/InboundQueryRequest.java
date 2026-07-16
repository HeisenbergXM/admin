package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "单车入库查询请求")
public class InboundQueryRequest extends PageRequest {

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "车型 ID")
    private Long modelId;

    @Schema(description = "入库开始日期")
    private LocalDate storageStartDate;

    @Schema(description = "入库结束日期")
    private LocalDate storageEndDate;

    @Schema(description = "阶段状态：PENDING_INBOUND 按车辆生命周期过滤，DRAFT/CONFIRMED 按入库记录状态过滤")
    private String stageStatus;
}

package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "运输单分页查询请求")
public class TransportOrderQueryRequest extends PageRequest {

    @Schema(description = "运输单号")
    private String orderNo;

    @Schema(description = "单据状态")
    private String orderStatus;

    @Schema(description = "到仓库开始日期")
    private LocalDate startDate;

    @Schema(description = "到仓库结束日期")
    private LocalDate endDate;
}

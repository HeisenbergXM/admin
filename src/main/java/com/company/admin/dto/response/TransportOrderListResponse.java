package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "运输单列表响应")
public class TransportOrderListResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "运输单号")
    private String orderNo;

    @Schema(description = "到仓库日期")
    private LocalDate dateToStorageYard;

    @Schema(description = "单据状态")
    private String orderStatus;
    @Schema(description = "单据状态中文名称")
    private String orderStatusLabel;

    @Schema(description = "VIN 数量")
    private Integer itemCount;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}

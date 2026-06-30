package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "运输单 VIN 明细请求")
public class TransportOrderItemRequest {

    @Schema(description = "车辆 ID")
    @NotNull(message = "车辆 ID 不能为空")
    private Long vehicleId;

    @Schema(description = "SAIC buy off 日期")
    @NotNull(message = "SAIC buy off 日期不能为空")
    private LocalDate saicBuyOffDate;
}

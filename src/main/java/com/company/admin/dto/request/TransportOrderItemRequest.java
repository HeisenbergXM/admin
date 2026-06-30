package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "运输单 VIN 明细请求")
public class TransportOrderItemRequest {

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "SAIC buy off 日期")
    private LocalDate saicBuyOffDate;
}

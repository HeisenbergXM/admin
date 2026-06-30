package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "运输单详情响应")
public class TransportOrderDetailResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "运输单号")
    private String orderNo;

    @Schema(description = "到仓库日期")
    private LocalDate dateToStorageYard;

    @Schema(description = "备注2")
    private String remark2;

    @Schema(description = "单据状态")
    private String orderStatus;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;

    @Schema(description = "VIN 明细")
    private List<Item> items = new ArrayList<>();

    @Data
    @Schema(description = "运输单 VIN 明细响应")
    public static class Item {
        @Schema(description = "明细 ID")
        private Long id;
        @Schema(description = "车辆 ID")
        private Long vehicleId;
        @Schema(description = "SAIC buy off 日期")
        private LocalDate saicBuyOffDate;
        @Schema(description = "车辆基本信息")
        private VehicleBasicInfo vehicle;
    }
}

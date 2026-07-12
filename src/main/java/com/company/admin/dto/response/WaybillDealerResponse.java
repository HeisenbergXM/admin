package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "路单经销商行响应")
public class WaybillDealerResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "行车路单 ID")
    private Long waybillId;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "发车日期")
    private LocalDate etdToDealer;

    @Schema(description = "预计到达日期")
    private LocalDate etaToDealer;

    @Schema(description = "签收日期")
    private LocalDate receivedDate;

    @Schema(description = "配送状态")
    private String deliveryStatus;
    @Schema(description = "配送状态中文名称")
    private String deliveryStatusLabel;

    @Schema(description = "备注7")
    private String remark7;

    @Schema(description = "行状态")
    private String rowStatus;
    @Schema(description = "行状态中文名称")
    private String rowStatusLabel;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;

    @Schema(description = "VIN 列表")
    private List<VehicleBasicInfo> vins = new ArrayList<>();
}

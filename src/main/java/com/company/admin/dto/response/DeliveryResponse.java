package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "单车配送响应")
public class DeliveryResponse {

    @Schema(description = "主键 ID；未创建配送记录时为空")
    private Long id;

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "当前生命周期阶段")
    private String lifecycleStage;

    @Schema(description = "当前生命周期阶段中文名称")
    private String lifecycleStageLabel;

    @Schema(description = "配送记录状态；未创建记录且车辆待配送时为 PENDING_DELIVERY")
    private String stageStatus;

    @Schema(description = "配送记录状态中文名称")
    private String stageStatusLabel;

    @Schema(description = "车型 ID")
    private Long modelId;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "车系")
    private String series;

    @Schema(description = "配置规格")
    private String spec;

    @Schema(description = "车型代码")
    private String modelCode;

    @Schema(description = "年款")
    private String yearMake;

    @Schema(description = "外饰颜色 ID")
    private Long exteriorColorId;

    @Schema(description = "外饰颜色名称")
    private String exteriorColorName;

    @Schema(description = "内饰颜色 ID")
    private Long interiorColorId;

    @Schema(description = "内饰颜色名称")
    private String interiorColorName;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "预计发往经销商日期")
    private LocalDate etdToDealer;

    @Schema(description = "预计到达经销商日期")
    private LocalDate etaToDealer;

    @Schema(description = "拖车类型")
    private String trollyType;

    @Schema(description = "是否满载")
    private Boolean fullyLoad;

    @Schema(description = "签收日期")
    private LocalDate receivedDate;

    @Schema(description = "配送状态")
    private String deliveryStatus;

    @Schema(description = "配送状态中文名称")
    private String deliveryStatusLabel;

    @Schema(description = "备注7")
    private String remark7;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;
}

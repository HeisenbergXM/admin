package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "单车入库响应")
public class InboundResponse {

    @Schema(description = "主键 ID；未创建入库记录时为空")
    private Long id;

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "当前生命周期阶段")
    private String lifecycleStage;

    @Schema(description = "当前生命周期阶段中文名称")
    private String lifecycleStageLabel;

    @Schema(description = "入库记录状态；未创建记录且车辆待入库时为 PENDING_INBOUND")
    private String stageStatus;

    @Schema(description = "入库记录状态中文名称")
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

    @Schema(description = "SAIC Buy Off 日期")
    private LocalDate saicBuyOffDate;

    @Schema(description = "入库日期")
    private LocalDate dateToStorageYard;

    @Schema(description = "备注2")
    private String remark2;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;
}

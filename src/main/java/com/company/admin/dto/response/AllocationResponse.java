package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "销售分配响应")
public class AllocationResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "阶段状态")
    private String stageStatus;

    @Schema(description = "当前生命周期阶段")
    private String lifecycleStage;

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

    @Schema(description = "分配日期")
    private LocalDate allocatedDate;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "销售状态")
    private String salesStatus;

    @Schema(description = "备注3")
    private String remark3;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;
}

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

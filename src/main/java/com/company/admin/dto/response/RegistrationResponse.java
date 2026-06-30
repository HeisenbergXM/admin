package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "上牌响应")
public class RegistrationResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "阶段状态")
    private String stageStatus;

    @Schema(description = "Drosstech 状态")
    private String drosstechStatus;

    @Schema(description = "上传日期")
    private LocalDate uploadDate;

    @Schema(description = "注册日期")
    private LocalDate registrationDate;

    @Schema(description = "客户区域")
    private String customerRegion;

    @Schema(description = "备注8")
    private String remark8;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;
}

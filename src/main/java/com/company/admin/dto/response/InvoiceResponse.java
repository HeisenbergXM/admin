package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "发票响应")
public class InvoiceResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "阶段状态")
    private String stageStatus;

    @Schema(description = "发票序号")
    private Integer invoiceSeq;

    @Schema(description = "发票类型")
    private String invoiceType;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "发票日期")
    private LocalDate invoiceDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;
}

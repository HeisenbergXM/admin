package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "发票列表响应")
public class InvoiceListResponse {

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "当前生命周期阶段")
    private String lifecycleStage;

    @Schema(description = "最新发票 ID")
    private Long latestInvoiceId;

    @Schema(description = "最新发票阶段状态")
    private String latestStageStatus;

    @Schema(description = "最新发票序号")
    private Integer latestInvoiceSeq;

    @Schema(description = "最新发票类型")
    private String latestInvoiceType;

    @Schema(description = "最新发票号")
    private String latestInvoiceNo;

    @Schema(description = "最新发票日期")
    private LocalDate latestInvoiceDate;

    @Schema(description = "是否持有有效正式发票")
    private Boolean hasFormalInvoice;

    @Schema(description = "发票记录数")
    private Integer invoiceCount;
}

package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "收款响应")
public class PaymentResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "阶段状态")
    private String stageStatus;

    @Schema(description = "收款日期")
    private LocalDate paymentDate;

    @Schema(description = "信用全款日期")
    private LocalDate creditFullPaymentDate;

    @Schema(description = "收款状态")
    private String paymentStatus;

    @Schema(description = "备注5")
    private String remark5;

    @Schema(description = "是否持有有效正式发票")
    private Boolean hasFormalInvoice;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;
}

package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "收款保存请求")
public class PaymentSaveRequest {

    @Schema(description = "收款日期")
    private LocalDate paymentDate;

    @Schema(description = "信用全款日期")
    private LocalDate creditFullPaymentDate;

    @Schema(description = "收款状态")
    private String paymentStatus;

    @Schema(description = "备注5")
    private String remark5;
}

package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "收款查询请求")
public class PaymentQueryRequest extends PageRequest {

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "收款状态")
    private String paymentStatus;

    @Schema(description = "阶段状态")
    private String stageStatus;

    @Schema(description = "是否持有有效正式发票")
    private Boolean hasFormalInvoice;
}

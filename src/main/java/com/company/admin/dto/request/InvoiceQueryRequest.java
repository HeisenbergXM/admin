package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "发票查询请求")
public class InvoiceQueryRequest extends PageRequest {

    @Schema(description = "VIN")
    private String vin;

    @Schema(description = "发票类型")
    private String invoiceType;

    @Schema(description = "是否持有有效正式发票")
    private Boolean hasFormalInvoice;
}

package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "发票确认请求")
public class InvoiceCreateRequest {

    @Schema(description = "发票类型：INVOICED=正式发票，PROFORMA_INVOICED=形式发票")
    private String invoiceType;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "发票日期")
    private LocalDate invoiceDate;

    @Schema(description = "备注")
    private String remark;
}

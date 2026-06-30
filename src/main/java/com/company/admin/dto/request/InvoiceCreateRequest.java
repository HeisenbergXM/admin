package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "发票确认请求")
public class InvoiceCreateRequest {

    @Schema(description = "发票类型：INVOICED=正式发票，PROFORMA_INVOICED=形式发票")
    @NotBlank(message = "发票类型不能为空")
    private String invoiceType;

    @Schema(description = "发票号")
    @NotBlank(message = "发票号不能为空")
    private String invoiceNo;

    @Schema(description = "发票日期")
    @NotNull(message = "发票日期不能为空")
    private LocalDate invoiceDate;

    @Schema(description = "备注")
    private String remark;
}

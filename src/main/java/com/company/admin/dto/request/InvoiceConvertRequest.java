package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "形式发票转正式请求")
public class InvoiceConvertRequest {

    @Schema(description = "正式发票号")
    private String invoiceNo;

    @Schema(description = "正式发票日期")
    private LocalDate invoiceDate;

    @Schema(description = "备注")
    private String remark;
}

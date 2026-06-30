package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "形式发票转正式请求")
public class InvoiceConvertRequest {

    @Schema(description = "正式发票号")
    @NotBlank(message = "正式发票号不能为空")
    private String invoiceNo;

    @Schema(description = "正式发票日期")
    @NotNull(message = "正式发票日期不能为空")
    private LocalDate invoiceDate;

    @Schema(description = "备注")
    private String remark;
}

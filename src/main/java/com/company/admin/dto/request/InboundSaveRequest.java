package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Schema(description = "单车入库草稿请求")
public class InboundSaveRequest {

    @Schema(description = "SAIC Buy Off 日期")
    private LocalDate saicBuyOffDate;

    @Schema(description = "入库日期")
    private LocalDate dateToStorageYard;

    @Schema(description = "备注2")
    @Size(max = 500, message = "备注2长度不能超过500")
    private String remark2;
}

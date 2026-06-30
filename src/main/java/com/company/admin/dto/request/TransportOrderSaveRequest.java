package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "运输单保存请求")
public class TransportOrderSaveRequest {

    @Schema(description = "运输单号")
    private String orderNo;

    @Schema(description = "到仓库日期")
    private LocalDate dateToStorageYard;

    @Schema(description = "备注2")
    private String remark2;
}

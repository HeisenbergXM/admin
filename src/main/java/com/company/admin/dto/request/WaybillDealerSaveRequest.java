package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "路单经销商行保存请求")
public class WaybillDealerSaveRequest {

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "发车日期")
    private LocalDate etdToDealer;

    @Schema(description = "预计到达日期")
    private LocalDate etaToDealer;

    @Schema(description = "签收日期")
    private LocalDate receivedDate;

    @Schema(description = "配送状态")
    private String deliveryStatus;

    @Schema(description = "备注7")
    private String remark7;
}

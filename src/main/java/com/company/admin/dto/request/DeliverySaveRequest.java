package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Schema(description = "单车配送草稿请求")
public class DeliverySaveRequest {

    @Schema(description = "预计发往经销商日期")
    private LocalDate etdToDealer;

    @Schema(description = "预计到达经销商日期")
    private LocalDate etaToDealer;

    @Schema(description = "拖车类型")
    private String trollyType;

    @Schema(description = "是否满载")
    private Boolean fullyLoad;

    @Schema(description = "签收日期")
    private LocalDate receivedDate;

    @Schema(description = "配送状态")
    private String deliveryStatus;

    @Schema(description = "备注7")
    @Size(max = 500, message = "备注7长度不能超过500")
    private String remark7;
}

package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "行车路单保存请求")
public class WaybillSaveRequest {

    @Schema(description = "路单号")
    private String waybillNo;

    @Schema(description = "轿运车类型")
    private String trollyType;

    @Schema(description = "是否满载：0=否，1=是")
    private Integer fullyLoad;
}

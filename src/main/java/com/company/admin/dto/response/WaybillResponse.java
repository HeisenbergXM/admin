package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "行车路单响应")
public class WaybillResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "路单号")
    private String waybillNo;

    @Schema(description = "发车清单 ID")
    private Long dispatchListId;

    @Schema(description = "轿运车类型")
    private String trollyType;

    @Schema(description = "是否满载")
    private Integer fullyLoad;

    @Schema(description = "经销商行")
    private List<WaybillDealerResponse> dealers = new ArrayList<>();
}

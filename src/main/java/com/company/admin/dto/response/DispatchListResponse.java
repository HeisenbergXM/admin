package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "发车清单响应")
public class DispatchListResponse {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "发车清单号")
    private String dispatchNo;

    @Schema(description = "清单状态")
    private String listStatus;
    @Schema(description = "清单状态中文名称")
    private String listStatusLabel;

    @Schema(description = "已签收经销商行数")
    private Integer confirmedDealerRows;

    @Schema(description = "未签收经销商行数")
    private Integer draftDealerRows;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "路单列表")
    private List<WaybillResponse> waybills = new ArrayList<>();
}

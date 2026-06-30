package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "销售分配保存请求")
public class AllocationSaveRequest {

    @Schema(description = "分配日期")
    private LocalDate allocatedDate;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "销售状态")
    private String salesStatus;

    @Schema(description = "备注3")
    private String remark3;
}

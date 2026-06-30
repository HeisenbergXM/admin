package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "发车清单查询请求")
public class DispatchListQueryRequest extends PageRequest {

    @Schema(description = "发车清单号")
    private String dispatchNo;
}

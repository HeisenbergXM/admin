package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发车清单保存请求")
public class DispatchListSaveRequest {

    @Schema(description = "发车清单号")
    private String dispatchNo;
}

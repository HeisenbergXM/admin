package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "启停状态更新请求")
public class StatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;
}

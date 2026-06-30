package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "上牌保存请求")
public class RegistrationSaveRequest {

    @Schema(description = "Drosstech 状态")
    private String drosstechStatus;

    @Schema(description = "上传日期")
    private LocalDate uploadDate;

    @Schema(description = "注册日期")
    private LocalDate registrationDate;

    @Schema(description = "客户区域")
    private String customerRegion;

    @Schema(description = "备注8")
    private String remark8;
}

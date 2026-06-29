package com.company.admin.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 生产阶段保存请求。
 */
@Data
public class ProductionSaveRequest {

    @NotBlank(message = "VIN 不能为空")
    @Size(min = 17, max = 17, message = "VIN 必须为 17 位")
    private String vin;

    private Long modelId;
    private Long exteriorColorId;
    private Long interiorColorId;
    private String engineNumber;
    private String yearMake;
    private String material;
    private String shipment;
    private String batch;
    private LocalDate offlineEpmbDate;
    private LocalDate epmbOkDate;
    private String remark1;
}

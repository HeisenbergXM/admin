package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 生产阶段保存请求。
 */
@Data
@Schema(description = "生产阶段保存请求")
public class ProductionSaveRequest {

    @NotBlank(message = "VIN 不能为空")
    @Size(min = 17, max = 17, message = "VIN 必须为 17 位")
    @Schema(description = "车辆识别码 VIN", example = "LSJW56U95RG000001")
    private String vin;

    @Schema(description = "车型 ID")
    private Long modelId;
    @Schema(description = "外饰颜色 ID")
    private Long exteriorColorId;
    @Schema(description = "内饰颜色 ID")
    private Long interiorColorId;
    @Schema(description = "发动机号")
    private String engineNumber;
    @Schema(description = "年款")
    private String yearMake;
    @Schema(description = "物料信息")
    private String material;
    @Schema(description = "Shipment 信息")
    private String shipment;
    @Schema(description = "批次")
    private String batch;
    @Schema(description = "Offline EPMB 日期")
    private LocalDate offlineEpmbDate;
    @Schema(description = "EPMB OK 日期")
    private LocalDate epmbOkDate;
    @Schema(description = "生产阶段备注")
    private String remark1;
}

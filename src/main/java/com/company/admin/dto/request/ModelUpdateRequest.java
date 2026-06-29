package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 车型编辑请求
 */
@Schema(description = "车型编辑请求")
public class ModelUpdateRequest {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键 ID")
    private Long id;

    @Size(max = 50, message = "物料编码长度不能超过50")
    @Schema(description = "物料编码")
    private String materialCode;

    @Size(max = 100, message = "车系长度不能超过100")
    @Schema(description = "车系")
    private String series;

    @Size(max = 100, message = "配置规格长度不能超过100")
    @Schema(description = "配置规格")
    private String spec;

    @Size(max = 100, message = "车型名称长度不能超过100")
    @Schema(description = "车型名称")
    private String modelName;

    @Size(max = 50, message = "车型代码长度不能超过50")
    @Schema(description = "车型代码")
    private String modelCode;

    @Size(max = 20, message = "年款长度不能超过20")
    @Schema(description = "年款")
    private String yearMake;

    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getYearMake() {
        return yearMake;
    }

    public void setYearMake(String yearMake) {
        this.yearMake = yearMake;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
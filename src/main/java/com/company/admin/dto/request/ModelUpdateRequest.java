package com.company.admin.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 车型编辑请求
 */
public class ModelUpdateRequest {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(max = 50, message = "物料编码长度不能超过50")
    private String materialCode;

    @Size(max = 100, message = "车系长度不能超过100")
    private String series;

    @Size(max = 100, message = "配置规格长度不能超过100")
    private String spec;

    @Size(max = 100, message = "车型名称长度不能超过100")
    private String modelName;

    @Size(max = 50, message = "车型代码长度不能超过50")
    private String modelCode;

    @Size(max = 20, message = "年款长度不能超过20")
    private String yearMake;

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
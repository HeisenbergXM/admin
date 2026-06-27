package com.company.admin.dto.request;

/**
 * 车型查询请求（分页）
 */
public class ModelQueryRequest extends PageRequest {

    /** 按物料编码模糊查询 */
    private String materialCode;

    /** 按车系精确查询 */
    private String series;

    /** 按配置规格模糊查询 */
    private String spec;

    /** 按车型名称模糊查询 */
    private String modelName;

    /** 按车型代码模糊查询 */
    private String modelCode;

    /** 按年款精确查询 */
    private String yearMake;

    /** 按状态筛选 */
    private Integer status;

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
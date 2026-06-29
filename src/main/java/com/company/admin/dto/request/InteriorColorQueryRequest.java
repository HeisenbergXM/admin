package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 内饰颜色查询请求（分页）
 */
@Schema(description = "内饰颜色分页查询请求")
public class InteriorColorQueryRequest extends PageRequest {

    /** 按颜色名称模糊查询（匹配中英文） */
    @Schema(description = "颜色名称")
    private String colorName;

    /** 按状态筛选 */
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
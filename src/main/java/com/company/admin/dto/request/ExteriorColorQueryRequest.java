package com.company.admin.dto.request;

/**
 * 外饰颜色查询请求（分页）
 */
public class ExteriorColorQueryRequest extends PageRequest {

    /** 按颜色名称模糊查询（匹配中英文） */
    private String colorName;

    /** 按状态筛选 */
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
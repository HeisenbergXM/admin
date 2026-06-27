package com.company.admin.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 内饰颜色创建请求
 */
public class InteriorColorCreateRequest {

    @NotBlank(message = "颜色名称不能为空")
    @Size(max = 100, message = "颜色名称长度不能超过100")
    private String colorName;

    @Size(max = 100, message = "中文名称长度不能超过100")
    private String colorNameCn;

    /** 状态：0停用 1正常，默认1 */
    private Integer status = 1;

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorNameCn() {
        return colorNameCn;
    }

    public void setColorNameCn(String colorNameCn) {
        this.colorNameCn = colorNameCn;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
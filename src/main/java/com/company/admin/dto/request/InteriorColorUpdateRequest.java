package com.company.admin.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 内饰颜色编辑请求
 */
public class InteriorColorUpdateRequest {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(max = 100, message = "颜色名称长度不能超过100")
    private String colorName;

    @Size(max = 100, message = "中文名称长度不能超过100")
    private String colorNameCn;

    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
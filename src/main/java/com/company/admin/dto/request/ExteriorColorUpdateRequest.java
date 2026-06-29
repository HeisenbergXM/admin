package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 外饰颜色编辑请求
 */
@Schema(description = "外饰颜色编辑请求")
public class ExteriorColorUpdateRequest {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键 ID")
    private Long id;

    @Size(max = 100, message = "颜色名称长度不能超过100")
    @Schema(description = "颜色名称")
    private String colorName;

    @Size(max = 100, message = "中文名称长度不能超过100")
    @Schema(description = "颜色中文名称")
    private String colorNameCn;

    @Schema(description = "状态：0=停用，1=正常", example = "1")
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
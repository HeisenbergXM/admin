package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 字典类型编辑请求
 */
@Schema(description = "字典类型编辑请求")
public class DictTypeUpdateRequest {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键 ID")
    private Long id;

    @Size(max = 100, message = "字典名称长度不能超过100")
    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

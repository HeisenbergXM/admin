package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

/**
 * 字典项创建/编辑请求
 */
@Schema(description = "字典项保存请求")
public class DictItemSaveRequest {

    /** 编辑时必填；创建时可空（创建时由路径参数传入 typeId） */
    @Schema(description = "主键 ID")
    private Long id;

    /** 字典类型 ID（创建时必填） */
    @Schema(description = "字典类型 ID")
    private Long dictTypeId;

    @NotBlank(message = "字典值不能为空")
    @Schema(description = "字典值")
    private String itemValue;

    @NotBlank(message = "字典标签不能为空")
    @Schema(description = "字典标签")
    private String itemLabel;

    /** 排序号，默认 0 */
    @Schema(description = "排序号")
    private Integer sortOrder = 0;

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

    public Long getDictTypeId() {
        return dictTypeId;
    }

    public void setDictTypeId(Long dictTypeId) {
        this.dictTypeId = dictTypeId;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

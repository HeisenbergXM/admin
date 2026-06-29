package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 字典类型查询请求（分页）
 */
@Schema(description = "字典类型分页查询请求")
public class DictTypeQueryRequest extends PageRequest {

    /** 按字典编码模糊查询 */
    @Schema(description = "字典编码")
    private String dictCode;

    /** 按字典名称模糊查询 */
    @Schema(description = "字典名称")
    private String dictName;

    /** 按状态筛选 */
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
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
}

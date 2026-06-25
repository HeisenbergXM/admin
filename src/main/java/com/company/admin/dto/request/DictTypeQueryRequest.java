package com.company.admin.dto.request;

/**
 * 字典类型查询请求（分页）
 */
public class DictTypeQueryRequest extends PageRequest {

    /** 按字典编码模糊查询 */
    private String dictCode;

    /** 按字典名称模糊查询 */
    private String dictName;

    /** 按状态筛选 */
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

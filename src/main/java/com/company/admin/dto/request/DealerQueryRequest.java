package com.company.admin.dto.request;

/**
 * 经销商查询请求（分页）
 */
public class DealerQueryRequest extends PageRequest {

    /** 按经销商编码模糊查询 */
    private String dealerCode;

    /** 按经销商名称模糊查询 */
    private String dealerName;

    /** 按状态筛选 */
    private Integer status;

    public String getDealerCode() {
        return dealerCode;
    }

    public void setDealerCode(String dealerCode) {
        this.dealerCode = dealerCode;
    }

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
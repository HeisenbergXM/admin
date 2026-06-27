package com.company.admin.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 经销商编辑请求
 */
public class DealerUpdateRequest {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(max = 50, message = "经销商编码长度不能超过50")
    private String dealerCode;

    @Size(max = 200, message = "经销商名称长度不能超过200")
    private String dealerName;

    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
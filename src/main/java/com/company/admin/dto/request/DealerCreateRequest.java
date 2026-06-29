package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 经销商创建请求
 */
@Schema(description = "经销商创建请求")
public class DealerCreateRequest {

    @NotBlank(message = "经销商编码不能为空")
    @Size(max = 50, message = "经销商编码长度不能超过50")
    @Schema(description = "经销商编码")
    private String dealerCode;

    @NotBlank(message = "经销商名称不能为空")
    @Size(max = 200, message = "经销商名称长度不能超过200")
    @Schema(description = "经销商名称")
    private String dealerName;

    /** 状态：0停用 1正常，默认1 */
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status = 1;

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
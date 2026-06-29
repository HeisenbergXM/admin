package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户分页查询请求")
public class UserQueryRequest extends PageRequest {
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

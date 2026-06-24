package com.company.admin.dto.request;

import javax.validation.constraints.NotNull;
import java.util.List;

public class UserRoleRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    private List<Long> roleIds;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}

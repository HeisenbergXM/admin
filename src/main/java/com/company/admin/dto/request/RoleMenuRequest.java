package com.company.admin.dto.request;

import javax.validation.constraints.NotNull;
import java.util.List;

public class RoleMenuRequest {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
    private List<Long> menuIds;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}

package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "角色菜单分配请求")
public class RoleMenuRequest {
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色 ID")
    private Long roleId;
    @Schema(description = "菜单 ID 列表")
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

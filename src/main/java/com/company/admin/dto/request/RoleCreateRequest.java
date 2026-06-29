package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Schema(description = "角色创建或编辑请求")
public class RoleCreateRequest {
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最长50个字符")
    @Schema(description = "角色名称")
    private String roleName;
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最长50个字符")
    @Schema(description = "角色编码")
    private String roleCode;
    @Size(max = 200, message = "描述最长200个字符")
    @Schema(description = "描述")
    private String description;
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

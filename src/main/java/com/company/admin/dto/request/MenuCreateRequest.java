package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "菜单创建或编辑请求")
public class MenuCreateRequest {
    @Schema(description = "父级 ID")
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称最长50个字符")
    @Schema(description = "菜单名称")
    private String menuName;
    @NotNull(message = "菜单类型不能为空")
    @Schema(description = "菜单类型：1=目录，2=菜单，3=按钮")
    private Integer menuType;
    @Schema(description = "路由路径")
    private String path;
    @Schema(description = "权限标识")
    private String permission;
    @Schema(description = "菜单图标")
    private String icon;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Integer getMenuType() {
        return menuType;
    }

    public void setMenuType(Integer menuType) {
        this.menuType = menuType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

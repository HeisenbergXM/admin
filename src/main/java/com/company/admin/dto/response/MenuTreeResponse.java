package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "菜单树节点")
public class MenuTreeResponse {
    @Schema(description = "主键 ID")
    private Long id;
    @Schema(description = "父级 ID")
    private Long parentId;
    @Schema(description = "菜单名称")
    private String menuName;
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
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "子菜单列表")
    private List<MenuTreeResponse> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<MenuTreeResponse> getChildren() {
        return children;
    }

    public void setChildren(List<MenuTreeResponse> children) {
        this.children = children;
    }
}

package com.company.admin.common;

public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),

    // 用户模块 1xxx
    USERNAME_PASSWORD_ERROR(1001, "用户名或密码错误"),
    USER_DISABLED(1002, "用户已禁用"),
    USERNAME_EXISTS(1003, "用户名已存在"),
    USER_OLD_PASSWORD_ERROR(1004, "原密码错误"),

    // 角色模块 2xxx
    ROLE_CODE_EXISTS(2001, "角色编码已存在"),

    // 菜单模块 3xxx
    MENU_NAME_EXISTS(3001, "菜单名称已存在"),
    MENU_HAS_CHILDREN(3002, "存在子菜单，无法删除"),

    // Token 模块 4xxx
    TOKEN_EXPIRED(4001, "Token 已过期"),
    TOKEN_INVALID(4002, "Token 无效"),

    // 系统错误 5xxx
    SYSTEM_ERROR(5000, "系统内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

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
    SYSTEM_ERROR(5000, "系统内部错误"),

    // VLM 车辆生命周期 6xxx
    VEHICLE_NOT_FOUND(6001, "车辆不存在"),
    VIN_DUPLICATE(6002, "VIN 已存在"),
    LIFECYCLE_STAGE_MISMATCH(6003, "生命周期阶段不匹配"),
    STAGE_ALREADY_CONFIRMED(6004, "阶段已确认，不可修改"),
    VEHICLE_OCCUPIED_BY_OTHER_ORDER(6005, "车辆已被其他单据占用"),
    INVOICE_NOT_FORMAL(6006, "未持有正式发票，不可收款"),
    STAGE_DATA_NOT_FOUND(6007, "阶段数据不存在"),
    DICT_CODE_EXISTS(6008, "字典编码已存在"),
    DICT_TYPE_HAS_ITEMS(6009, "字典类型下存在字典项，无法删除"),
    DEALER_CODE_EXISTS(6010, "经销商编码已存在"),
    TRANSPORT_ORDER_NOT_FOUND(6011, "运输单不存在"),
    DISPATCH_LIST_NOT_FOUND(6012, "发车清单不存在"),
    WAYBILL_NOT_FOUND(6013, "行车路单不存在"),
    WAYBILL_DEALER_NOT_FOUND(6014, "经销商行不存在"),
    DICT_TYPE_NOT_FOUND(6015, "字典类型不存在"),
    DICT_ITEM_NOT_FOUND(6016, "字典项不存在"),
    DICT_ITEM_ID_REQUIRED(6017, "字典项 ID 不能为空");

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

package com.company.admin.enums;

/**
 * 物流单据状态（运输单、发车清单、经销商行等通用）
 */
public enum OrderStatus {

    DRAFT("草稿"),
    CONFIRMED("已确认");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

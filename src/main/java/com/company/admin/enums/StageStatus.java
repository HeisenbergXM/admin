package com.company.admin.enums;

/**
 * 阶段记录状态
 */
public enum StageStatus {

    DRAFT("草稿"),
    CONFIRMED("已确认");

    private final String label;

    StageStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package com.company.admin.enums;

/**
 * 车辆生命周期阶段枚举
 * 状态名采用「待办态」视角——表达该车当前等待什么操作
 */
public enum LifecycleStage {

    PENDING_OFFLINE("草稿（新录入）"),
    PENDING_INBOUND("待入库"),
    PENDING_ALLOCATION("待分配"),
    PENDING_INVOICE("待开票"),
    PENDING_PAYMENT("待收款"),
    PENDING_DELIVERY("待配送"),
    PENDING_REGISTRATION("待上牌"),
    COMPLETED("已完结");

    private final String label;

    LifecycleStage(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 获取下一个阶段，COMPLETED 无下一阶段返回 null
     */
    public LifecycleStage next() {
        LifecycleStage[] stages = values();
        int nextOrdinal = this.ordinal() + 1;
        if (nextOrdinal >= stages.length) {
            return null;
        }
        return stages[nextOrdinal];
    }
}

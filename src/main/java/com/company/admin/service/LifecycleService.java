package com.company.admin.service;

import com.company.admin.enums.LifecycleStage;

/**
 * 生命周期卡控引擎 —— 所有阶段操作的公共依赖
 * <p>
 * 各阶段 Service 仅通过此接口进行阶段校验和推进，互不直接依赖。
 */
public interface LifecycleService {

    /**
     * 获取车辆当前生命周期阶段
     *
     * @throws com.company.admin.common.BusinessException 车辆不存在时抛 VEHICLE_NOT_FOUND
     */
    LifecycleStage getCurrentStage(Long vehicleId);

    /**
     * 校验车辆是否处于指定阶段，不匹配抛 LIFECYCLE_STAGE_MISMATCH
     */
    void assertStage(Long vehicleId, LifecycleStage expected);

    /**
     * 推进车辆生命周期阶段（在调用方事务内执行）
     *
     * @param from 当前阶段（二次校验）
     * @param to   目标阶段
     */
    void advanceStage(Long vehicleId, LifecycleStage from, LifecycleStage to);

    /**
     * 校验阶段记录未确认（DRAFT 状态），已确认则抛 STAGE_ALREADY_CONFIRMED
     */
    void assertNotConfirmed(String stageStatus);

    /**
     * 确认阶段记录：锁定阶段数据 + 推进主表生命周期
     *
     * @param vehicleId       车辆 ID
     * @param stageStatus     当前阶段记录状态（用于二次校验）
     * @param from            当前阶段
     * @param to              目标阶段
     * @param lockAction      锁定阶段记录的具体操作（设 confirmed_by/at、stage_status=CONFIRMED、update DB）
     */
    void confirmAndAdvance(Long vehicleId, String stageStatus,
                           LifecycleStage from, LifecycleStage to,
                           Runnable lockAction);

    /**
     * 校验车辆是否持有有效正式发票（收款前置条件）
     * 有效发票 = seq 最大且 invoice_type 为正式发票的记录
     */
    boolean hasValidFormalInvoice(Long vehicleId);
}

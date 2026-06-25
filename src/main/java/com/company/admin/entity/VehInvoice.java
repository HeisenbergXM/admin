package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发票阶段表（1:N，一车至多两条：seq=1 首次开票 + seq=2 转正记录）
 */
@Data
@TableName("t_veh_invoice")
public class VehInvoice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK t_vehicle */
    private Long vehicleId;

    /** 阶段记录状态：DRAFT / CONFIRMED */
    private String stageStatus;

    /** 序号：1=首次开票，2=转正记录 */
    private Integer invoiceSeq;

    /** 发票种类（正式 INVOICED / 形式 PROFORMA_INVOICED） */
    private String invoiceType;

    /** 发票号 */
    private String invoiceNo;

    /** 发票日期 */
    private LocalDate invoiceDate;

    /** 备注（remark4 / remark6） */
    private String remark;

    // ---- 确认审计字段 ----
    private String confirmedBy;
    private LocalDateTime confirmedAt;

    // ---- 创建/修改审计字段 ----
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

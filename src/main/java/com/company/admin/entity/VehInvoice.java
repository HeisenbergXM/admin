package com.company.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发票阶段表（1:N，一车至多两条：seq=1 首次开票 + seq=2 转正记录）
 */
@Data
@TableName("t_veh_invoice")
@Schema(description = "发票阶段记录")
public class VehInvoice {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    /** FK t_vehicle */
    @Schema(description = "车辆 ID")
    private Long vehicleId;

    /** 阶段记录状态：DRAFT / CONFIRMED */
    @Schema(description = "阶段记录状态：DRAFT=草稿，CONFIRMED=已确认")
    private String stageStatus;

    /** 序号：1=首次开票，2=转正记录 */
    @Schema(description = "发票序号：1=首次开票，2=转正记录")
    private Integer invoiceSeq;

    /** 发票种类（正式 INVOICED / 形式 PROFORMA_INVOICED） */
    @Schema(description = "发票类型")
    private String invoiceType;

    /** 发票号 */
    @Schema(description = "发票号")
    private String invoiceNo;

    /** 发票日期 */
    @Schema(description = "发票日期")
    private LocalDate invoiceDate;

    /** 备注（remark4 / remark6） */
    @Schema(description = "备注")
    private String remark;

    // ---- 确认审计字段 ----
    @Schema(description = "确认人")
    private String confirmedBy;
    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;

    // ---- 创建/修改审计字段 ----
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人")
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableLogic
    @Schema(description = "逻辑删除标记：0=未删除，1=已删除")
    private Integer deleted;
}

package com.company.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典项（字典类型下的具体值，如 invoice_status 下的 INVOICED/NOT_INVOICED）
 */
@Data
@TableName("t_sys_dict_item")
@Schema(description = "字典项")
public class DictItem {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    /** 所属字典类型 ID */
    @Schema(description = "字典类型 ID")
    private Long dictTypeId;

    /** 字典值（存储值） */
    @Schema(description = "字典值")
    private String itemValue;

    /** 字典标签（展示文本） */
    @Schema(description = "字典标签")
    private String itemLabel;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /** 状态：0停用 1正常 */
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "逻辑删除标记：0=未删除，1=已删除")
    private Integer deleted;
}

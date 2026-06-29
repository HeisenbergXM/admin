package com.company.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型（如车型状态、发票类型等）
 */
@Data
@TableName("t_sys_dict_type")
@Schema(description = "字典类型")
public class DictType {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    /** 字典编码（唯一标识，如 invoice_status） */
    @Schema(description = "字典编码")
    private String dictCode;

    /** 字典名称 */
    @Schema(description = "字典名称")
    private String dictName;

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

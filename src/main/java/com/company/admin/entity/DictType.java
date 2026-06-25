package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型（如车型状态、发票类型等）
 */
@Data
@TableName("t_sys_dict_type")
public class DictType {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典编码（唯一标识，如 invoice_status） */
    private String dictCode;

    /** 字典名称 */
    private String dictName;

    /** 状态：0停用 1正常 */
    private Integer status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

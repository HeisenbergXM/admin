package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典项（字典类型下的具体值，如 invoice_status 下的 INVOICED/NOT_INVOICED）
 */
@Data
@TableName("t_sys_dict_item")
public class DictItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属字典类型 ID */
    private Long dictTypeId;

    /** 字典值（存储值） */
    private String itemValue;

    /** 字典标签（展示文本） */
    private String itemLabel;

    /** 排序号 */
    private Integer sortOrder;

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

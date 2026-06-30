package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("t_waybill")
@Schema(description = "行车路单")
public class Waybill {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "路单号")
    private String waybillNo;

    @Schema(description = "发车清单 ID")
    private Long dispatchListId;

    @Schema(description = "轿运车类型")
    private String trollyType;

    @Schema(description = "是否满载：0=否，1=是")
    private Integer fullyLoad;
}

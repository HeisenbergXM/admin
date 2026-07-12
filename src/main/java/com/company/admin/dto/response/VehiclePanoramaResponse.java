package com.company.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "车辆全景视图响应")
public class VehiclePanoramaResponse {

    @Schema(description = "车辆基本信息")
    private VehicleBasicInfo vehicle;

    @Schema(description = "生命周期时间线")
    private List<TimelineNode> timeline = new ArrayList<>();

    @Schema(description = "生产阶段")
    private ProductionResponse production;

    @Schema(description = "车厂到仓库运输单")
    private TransportOrderDetailResponse transportOrder;

    @Schema(description = "销售分配")
    private AllocationResponse allocation;

    @Schema(description = "发票记录")
    private List<InvoiceResponse> invoices = new ArrayList<>();

    @Schema(description = "收款记录")
    private PaymentResponse payment;

    @Schema(description = "仓库到经销商配送")
    private DispatchListResponse dispatch;

    @Schema(description = "上牌记录")
    private RegistrationResponse registration;

    @Data
    @Schema(description = "生命周期时间线节点")
    public static class TimelineNode {
        @Schema(description = "阶段")
        private String stage;
        @Schema(description = "阶段中文名称")
        private String stageLabel;
        @Schema(description = "阶段名称")
        private String name;
        @Schema(description = "确认人")
        private String confirmedBy;
        @Schema(description = "确认时间")
        private LocalDateTime confirmedAt;
    }
}

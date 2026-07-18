package com.company.admin.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * 管理员车辆数据修订请求；仅包含允许修订的业务字段。
 */
@Data
public class VehicleCorrectionUpdateRequest {

    @Valid
    private ProductionCorrection production;

    @Valid
    private InboundCorrection inbound;

    @Valid
    private AllocationCorrection allocation;

    @Valid
    private List<InvoiceCorrection> invoices;

    @Valid
    private PaymentCorrection payment;

    @Valid
    private DeliveryCorrection delivery;

    @Valid
    private RegistrationCorrection registration;

    @Data
    public static class ProductionCorrection {
        @NotNull
        private Long id;
        @NotNull
        private Long modelId;
        @NotNull
        private Long exteriorColorId;
        @NotNull
        private Long interiorColorId;
        @NotBlank
        @Size(max = 50)
        private String engineNumber;
        @Size(max = 20)
        private String yearMake;
        @Size(max = 100)
        private String material;
        @Size(max = 100)
        private String shipment;
        @Size(max = 100)
        private String batch;
        private LocalDate offlineEpmbDate;
        private LocalDate epmbOkDate;
        @Size(max = 500)
        private String remark1;
    }

    @Data
    public static class InboundCorrection {
        @NotNull
        private Long id;
        private LocalDate saicBuyOffDate;
        private LocalDate dateToStorageYard;
        @Size(max = 500)
        private String remark2;
    }

    @Data
    public static class AllocationCorrection {
        @NotNull
        private Long id;
        private LocalDate allocatedDate;
        @NotNull
        private Long dealerId;
        @Size(max = 50)
        private String salesStatus;
        @Size(max = 500)
        private String remark3;
    }

    @Data
    public static class InvoiceCorrection {
        @NotNull
        private Long id;
        @NotBlank
        @Size(max = 50)
        private String invoiceType;
        @NotBlank
        @Size(max = 100)
        private String invoiceNo;
        @NotNull
        private LocalDate invoiceDate;
        @Size(max = 500)
        private String remark;
    }

    @Data
    public static class PaymentCorrection {
        @NotNull
        private Long id;
        @NotNull
        private LocalDate paymentDate;
        private LocalDate creditFullPaymentDate;
        @Size(max = 50)
        private String paymentStatus;
        @Size(max = 500)
        private String remark5;
    }

    @Data
    public static class DeliveryCorrection {
        @NotNull
        private Long id;
        private LocalDate etdToDealer;
        private LocalDate etaToDealer;
        @Size(max = 20)
        private String trollyType;
        private Boolean fullyLoad;
        private LocalDate receivedDate;
        @Size(max = 50)
        private String deliveryStatus;
        @Size(max = 500)
        private String remark7;
    }

    @Data
    public static class RegistrationCorrection {
        @NotNull
        private Long id;
        @Size(max = 50)
        private String drosstechStatus;
        private LocalDate uploadDate;
        private LocalDate registrationDate;
        @Size(max = 100)
        private String customerRegion;
        @Size(max = 500)
        private String remark8;
    }
}

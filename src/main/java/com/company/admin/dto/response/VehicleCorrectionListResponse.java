package com.company.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Vehicle correction master-sheet row")
@JsonPropertyOrder({"id", "no", "model", "exteriorColor", "interiorColor", "vinNumber",
        "engineNumber", "modelCode", "yearMake", "material", "shipment", "batch",
        "offlineEpmb", "epmbOk", "remark1", "saicBuyOff", "dateToStorageYard", "remark2",
        "allocatedDate", "dealerCode", "dealer", "remark3", "status1", "invoiceNo1",
        "invoiceDate1", "remark4", "paymentDate", "creditFullPaymentDate", "paymentStatus",
        "remark5", "status2", "invoiceNo2", "invoiceDate2", "remark6", "etdToDealer",
        "etaToDealer", "trollyType", "fullyLoad", "receivedDateByDealer", "deliveryStatus",
        "remark7", "drosstechStatus", "uploadDate", "registration", "customerRegion", "remark8"})
public class VehicleCorrectionListResponse {
    private Long id;
    @Schema(description = "NO.")
    private Long no;
    @Schema(description = "MODEL")
    private String model;
    @Schema(description = "EXTERIOR COLOR")
    private String exteriorColor;
    @Schema(description = "INTERIOR COLOR")
    private String interiorColor;
    @Schema(description = "VIN NUMBER")
    private String vinNumber;
    @Schema(description = "ENGINE NUMBER")
    private String engineNumber;
    @Schema(description = "MODEL CODE")
    private String modelCode;
    @Schema(description = "Year Make")
    private String yearMake;
    @Schema(description = "Material")
    private String material;
    @Schema(description = "Shipment")
    private String shipment;
    @Schema(description = "Batch ")
    private String batch;
    @Schema(description = "Offline EPMB")
    private LocalDate offlineEpmb;
    @Schema(description = "EPMB ok ")
    private LocalDate epmbOk;
    @Schema(description = "Remark1")
    private String remark1;
    @Schema(description = "SAIC buy off ")
    private LocalDate saicBuyOff;
    @Schema(description = "Date to Strogare Yard")
    private LocalDate dateToStorageYard;
    @Schema(description = "remark2")
    private String remark2;
    @Schema(description = "Allocated Date")
    private LocalDate allocatedDate;
    @Schema(description = "Dealer Code")
    private String dealerCode;
    @Schema(description = "Dealer")
    private String dealer;
    @Schema(description = "Remark3")
    private String remark3;
    @Schema(description = "Status1")
    private String status1;
    @Schema(description = "Invoice#")
    private String invoiceNo1;
    @Schema(description = "Invoice Date")
    private LocalDate invoiceDate1;
    @Schema(description = "remark4")
    private String remark4;
    @Schema(description = "Payment Date")
    private LocalDate paymentDate;
    @Schema(description = "Credit Full Payment Date")
    private LocalDate creditFullPaymentDate;
    @Schema(description = "Payment Status")
    private String paymentStatus;
    @Schema(description = "remark5")
    private String remark5;
    @Schema(description = "Status2")
    private String status2;
    @Schema(description = "Invoice#")
    private String invoiceNo2;
    @Schema(description = "Invoice Date")
    private LocalDate invoiceDate2;
    @Schema(description = "remark6")
    private String remark6;
    @Schema(description = "ETD  to Dealer")
    private LocalDate etdToDealer;
    @Schema(description = "ETA to Dealer ")
    private LocalDate etaToDealer;
    @Schema(description = "Trolly type\n4 units/ 6units")
    private String trollyType;
    @Schema(description = "Fully load or not")
    private String fullyLoad;
    @JsonIgnore
    private Boolean fullyLoadValue;
    @Schema(description = "Received date by Dealer ")
    private LocalDate receivedDateByDealer;
    @Schema(description = "Delivery Status")
    private String deliveryStatus;
    @Schema(description = "remark7")
    private String remark7;
    @Schema(description = "Drosstech Status")
    private String drosstechStatus;
    @Schema(description = "Upload Date")
    private LocalDate uploadDate;
    @Schema(description = "Registration")
    private LocalDate registration;
    @Schema(description = "Customer region")
    private String customerRegion;
    @Schema(description = "remark8")
    private String remark8;
}

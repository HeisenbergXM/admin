package com.company.admin.dto.request;

import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleCorrectionUpdateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requestContractDoesNotExposeProtectedFields() {
        Set<String> forbidden = Set.of("vin", "vehicleId", "lifecycleStage", "stageStatus",
                "invoiceSeq", "confirmedBy", "confirmedAt", "createdBy", "createdAt",
                "updatedBy", "updatedAt", "deleted");
        List<Class<?>> contracts = List.of(VehicleCorrectionUpdateRequest.class,
                VehicleCorrectionUpdateRequest.ProductionCorrection.class,
                VehicleCorrectionUpdateRequest.InboundCorrection.class,
                VehicleCorrectionUpdateRequest.AllocationCorrection.class,
                VehicleCorrectionUpdateRequest.InvoiceCorrection.class,
                VehicleCorrectionUpdateRequest.PaymentCorrection.class,
                VehicleCorrectionUpdateRequest.DeliveryCorrection.class,
                VehicleCorrectionUpdateRequest.RegistrationCorrection.class);

        for (Class<?> contract : contracts) {
            Set<String> fields = Arrays.stream(contract.getDeclaredFields())
                    .map(Field::getName).collect(Collectors.toSet());
            assertTrue(Collections.disjoint(fields, forbidden), contract.getSimpleName());
        }
    }

    @Test
    void nestedRecordsRequireIdsAndRequiredBusinessFields() {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setProduction(new VehicleCorrectionUpdateRequest.ProductionCorrection());
        request.setInvoices(List.of(new VehicleCorrectionUpdateRequest.InvoiceCorrection()));

        Set<String> paths = validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());

        assertTrue(paths.contains("production.id"));
        assertTrue(paths.contains("production.modelId"));
        assertTrue(paths.contains("production.exteriorColorId"));
        assertTrue(paths.contains("production.interiorColorId"));
        assertTrue(paths.contains("production.engineNumber"));
        assertTrue(paths.contains("invoices[0].id"));
        assertTrue(paths.contains("invoices[0].invoiceType"));
        assertTrue(paths.contains("invoices[0].invoiceNo"));
        assertTrue(paths.contains("invoices[0].invoiceDate"));
    }

    @Test
    void rejectsCorrectionStringsLongerThanDatabaseColumns() {
        VehicleCorrectionUpdateRequest request = correctionRequestWithStrings(
                "x".repeat(51), "x".repeat(21), "x".repeat(101), "x".repeat(101), "x".repeat(101),
                "x".repeat(51), "x".repeat(51), "x".repeat(101), "x".repeat(51), "x".repeat(21),
                "x".repeat(51), "x".repeat(51));

        Set<String> paths = validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());

        assertEquals(Set.of(
                "production.engineNumber", "production.yearMake", "production.material", "production.shipment",
                "production.batch", "allocation.salesStatus", "invoices[0].invoiceType", "invoices[0].invoiceNo",
                "payment.paymentStatus", "delivery.trollyType", "delivery.deliveryStatus", "registration.drosstechStatus"), paths);
    }

    @Test
    void acceptsCorrectionStringsAtDatabaseColumnMaximums() {
        VehicleCorrectionUpdateRequest request = correctionRequestWithStrings(
                "x".repeat(50), "x".repeat(20), "x".repeat(100), "x".repeat(100), "x".repeat(100),
                "x".repeat(50), "x".repeat(50), "x".repeat(100), "x".repeat(50), "x".repeat(20),
                "x".repeat(50), "x".repeat(50));

        assertTrue(validator.validate(request).isEmpty());
    }

    private VehicleCorrectionUpdateRequest correctionRequestWithStrings(
            String engineNumber, String yearMake, String material, String shipment, String batch, String salesStatus,
            String invoiceType, String invoiceNo, String paymentStatus, String trollyType, String deliveryStatus,
            String drosstechStatus) {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();

        VehicleCorrectionUpdateRequest.ProductionCorrection production = new VehicleCorrectionUpdateRequest.ProductionCorrection();
        production.setId(1L);
        production.setModelId(2L);
        production.setExteriorColorId(3L);
        production.setInteriorColorId(4L);
        production.setEngineNumber(engineNumber);
        production.setYearMake(yearMake);
        production.setMaterial(material);
        production.setShipment(shipment);
        production.setBatch(batch);
        request.setProduction(production);

        VehicleCorrectionUpdateRequest.AllocationCorrection allocation = new VehicleCorrectionUpdateRequest.AllocationCorrection();
        allocation.setId(5L);
        allocation.setDealerId(6L);
        allocation.setSalesStatus(salesStatus);
        request.setAllocation(allocation);

        VehicleCorrectionUpdateRequest.InvoiceCorrection invoice = new VehicleCorrectionUpdateRequest.InvoiceCorrection();
        invoice.setId(7L);
        invoice.setInvoiceType(invoiceType);
        invoice.setInvoiceNo(invoiceNo);
        invoice.setInvoiceDate(java.time.LocalDate.of(2026, 7, 18));
        request.setInvoices(List.of(invoice));

        VehicleCorrectionUpdateRequest.PaymentCorrection payment = new VehicleCorrectionUpdateRequest.PaymentCorrection();
        payment.setId(8L);
        payment.setPaymentDate(java.time.LocalDate.of(2026, 7, 18));
        payment.setPaymentStatus(paymentStatus);
        request.setPayment(payment);

        VehicleCorrectionUpdateRequest.DeliveryCorrection delivery = new VehicleCorrectionUpdateRequest.DeliveryCorrection();
        delivery.setId(9L);
        delivery.setTrollyType(trollyType);
        delivery.setDeliveryStatus(deliveryStatus);
        request.setDelivery(delivery);

        VehicleCorrectionUpdateRequest.RegistrationCorrection registration = new VehicleCorrectionUpdateRequest.RegistrationCorrection();
        registration.setId(10L);
        registration.setDrosstechStatus(drosstechStatus);
        request.setRegistration(registration);
        return request;
    }
}

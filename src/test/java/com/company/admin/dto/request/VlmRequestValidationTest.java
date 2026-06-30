package com.company.admin.dto.request;

import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VlmRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void productionSaveRequiresVehicleMasterDataAndEngineNumber() {
        ProductionSaveRequest request = new ProductionSaveRequest();
        request.setVin("LSJW56U95RG000001");

        Set<String> fields = invalidFields(request);

        assertTrue(fields.contains("modelId"));
        assertTrue(fields.contains("exteriorColorId"));
        assertTrue(fields.contains("interiorColorId"));
        assertTrue(fields.contains("engineNumber"));
    }

    @Test
    void invoiceCreateRequiresTypeNumberAndDate() {
        InvoiceCreateRequest request = new InvoiceCreateRequest();

        Set<String> fields = invalidFields(request);

        assertTrue(fields.contains("invoiceType"));
        assertTrue(fields.contains("invoiceNo"));
        assertTrue(fields.contains("invoiceDate"));
    }

    @Test
    void transportOrderItemRequiresVehicleAndSaicBuyOffDate() {
        TransportOrderItemRequest request = new TransportOrderItemRequest();

        Set<String> fields = invalidFields(request);

        assertTrue(fields.contains("vehicleId"));
        assertTrue(fields.contains("saicBuyOffDate"));
    }

    @Test
    void paymentRequiresPaymentDate() {
        PaymentSaveRequest request = new PaymentSaveRequest();

        assertTrue(invalidFields(request).contains("paymentDate"));
    }

    @Test
    void validCoreRequestsPassValidation() {
        ProductionSaveRequest production = new ProductionSaveRequest();
        production.setVin("LSJW56U95RG000001");
        production.setModelId(1L);
        production.setExteriorColorId(1L);
        production.setInteriorColorId(1L);
        production.setEngineNumber("ENG-001");

        InvoiceCreateRequest invoice = new InvoiceCreateRequest();
        invoice.setInvoiceType("INVOICED");
        invoice.setInvoiceNo("INV-001");
        invoice.setInvoiceDate(LocalDate.of(2026, 7, 1));

        TransportOrderItemRequest item = new TransportOrderItemRequest();
        item.setVehicleId(1L);
        item.setSaicBuyOffDate(LocalDate.of(2026, 7, 1));

        PaymentSaveRequest payment = new PaymentSaveRequest();
        payment.setPaymentDate(LocalDate.of(2026, 7, 2));

        assertTrue(validator.validate(production).isEmpty());
        assertTrue(validator.validate(invoice).isEmpty());
        assertTrue(validator.validate(item).isEmpty());
        assertTrue(validator.validate(payment).isEmpty());
    }

    private Set<String> invalidFields(Object request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}

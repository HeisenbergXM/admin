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
}

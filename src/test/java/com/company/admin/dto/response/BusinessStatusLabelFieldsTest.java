package com.company.admin.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessStatusLabelFieldsTest {

    @Test
    void responseDtosExposeLabelFieldsWithoutChangingCodeFields() {
        VehicleBasicInfo vehicle = new VehicleBasicInfo();
        vehicle.setLifecycleStage("PENDING_PAYMENT");
        vehicle.setLifecycleStageLabel("待收款");

        PaymentResponse payment = new PaymentResponse();
        payment.setStageStatus("DRAFT");
        payment.setStageStatusLabel("草稿");

        assertEquals("PENDING_PAYMENT", vehicle.getLifecycleStage());
        assertEquals("待收款", vehicle.getLifecycleStageLabel());
        assertEquals("DRAFT", payment.getStageStatus());
        assertEquals("草稿", payment.getStageStatusLabel());
    }
}

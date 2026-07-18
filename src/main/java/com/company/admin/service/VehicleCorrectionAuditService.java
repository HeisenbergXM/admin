package com.company.admin.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

public interface VehicleCorrectionAuditService {

    void recordSuccess(CorrectionDiff diff);

    @Data
    @RequiredArgsConstructor
    class CorrectionDiff {
        private final Long vehicleId;
        private final String vin;
        private final Map<String, Map<String, Object>> before = new LinkedHashMap<>();
        private final Map<String, Map<String, Object>> after = new LinkedHashMap<>();

        public void before(String stage, Long id, Map<String, Object> values) {
            before.put(stage + ":" + id, new LinkedHashMap<>(values));
        }

        public void after(String stage, Long id, Map<String, Object> values) {
            after.put(stage + ":" + id, new LinkedHashMap<>(values));
        }
    }
}

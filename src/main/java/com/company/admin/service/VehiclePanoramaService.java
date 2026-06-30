package com.company.admin.service;

import com.company.admin.dto.response.VehiclePanoramaResponse;

public interface VehiclePanoramaService {

    VehiclePanoramaResponse getPanorama(String vin);

    byte[] exportPanorama(String vin);
}

package com.botanicalvault.backend.dto.request;

import java.util.List;

import com.botanicalvault.backend.constants.LightExposure;

public interface PlantFieldsRequest {
    String commonName();

    String scientificName();

    Long typeId();

    Long sizeId();

    List<Long> originIds();

    List<LightExposure> lightExposures();

    String notes();
}

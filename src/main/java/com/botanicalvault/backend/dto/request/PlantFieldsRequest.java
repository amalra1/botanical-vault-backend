package com.botanicalvault.backend.dto.request;

import java.util.List;

public interface PlantFieldsRequest {
    String commonName();

    String scientificName();

    Long typeId();

    Long sizeId();

    List<Long> originIds();

    String notes();
}

package com.botanicalvault.backend.service;

import com.botanicalvault.backend.dto.request.PlantFieldsRequest;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.util.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlantFields {
    private final PlantReferenceResolver references;

    public void apply(Plant plant, PlantFieldsRequest request) {
        plant.setCommonName(request.commonName().trim());
        plant.setScientificName(Texts.trimToNull(request.scientificName()));
        plant.setType(references.type(request.typeId()));
        plant.setSize(references.size(request.sizeId()));
        plant.getOrigins().clear();
        plant.getOrigins().addAll(references.origins(request.originIds()));
        plant.getLightExposures().clear();
        plant.getLightExposures().addAll(request.lightExposures());
        plant.setNotes(Texts.trimToNull(request.notes()));
    }
}

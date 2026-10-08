package com.botanicalvault.backend.service;

import java.util.Objects;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.PlantSearchRequestDTO;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.util.SearchTexts;

public record PlantSearchCriteria(
    String query,
    Long typeId,
    Long originId,
    boolean unclassified,
    Long sizeId,
    Long viewId
) {
    public static PlantSearchCriteria from(PlantSearchRequestDTO dto) {
        if (dto.originId() != null && dto.unclassified()) {
            throw new BusinessException(ErrorCode.CONFLICTING_FILTERS, "Use either originId or unclassified, not both");
        }
        return new PlantSearchCriteria(
                SearchTexts.normalize(dto.q()),
                dto.typeId(),
                dto.originId(),
                dto.unclassified(),
                dto.sizeId(),
                dto.viewId());
    }

    public boolean matches(Plant plant) {
        return matchesQuery(plant)
                && matchesType(plant)
                && matchesOrigin(plant)
                && matchesSize(plant)
                && matchesView(plant);
    }

    public PlantSearchCriteria withoutType() {
        return new PlantSearchCriteria(query, null, originId, unclassified, sizeId, viewId);
    }

    public PlantSearchCriteria withoutOrigin() {
        return new PlantSearchCriteria(query, typeId, null, false, sizeId, viewId);
    }

    public PlantSearchCriteria withoutSize() {
        return new PlantSearchCriteria(query, typeId, originId, unclassified, null, viewId);
    }

    public PlantSearchCriteria withoutView() {
        return new PlantSearchCriteria(query, typeId, originId, unclassified, sizeId, null);
    }

    private boolean matchesQuery(Plant plant) {
        return query == null
                || SearchTexts.contains(plant.getCommonName(), query)
                || SearchTexts.contains(plant.getScientificName(), query);
    }

    private boolean matchesType(Plant plant) {
        return typeId == null || Objects.equals(plant.getType().getId(), typeId);
    }

    private boolean matchesOrigin(Plant plant) {
        if (unclassified) {
            return plant.getOrigins().isEmpty();
        }
        return originId == null
                || plant.getOrigins().stream().anyMatch(origin -> Objects.equals(origin.getId(), originId));
    }

    private boolean matchesSize(Plant plant) {
        return sizeId == null || Objects.equals(plant.getSize().getId(), sizeId);
    }

    private boolean matchesView(Plant plant) {
        return viewId == null
                || plant.getImages().stream().anyMatch(image -> Objects.equals(image.getView().getId(), viewId));
    }
}

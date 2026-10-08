package com.botanicalvault.backend.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.botanicalvault.backend.dto.response.SizeCoverageResponseDTO;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.Size;

public final class SizeCoverage {

    private SizeCoverage() {
    }

    public static List<SizeCoverageResponseDTO> of(List<Size> sizes, Collection<Plant> plants) {
        Set<Long> coveredIds = plants.stream().map(plant -> plant.getSize().getId()).collect(Collectors.toSet());
        return sizes.stream()
                .map(size -> new SizeCoverageResponseDTO(
                        size.getId(), size.getName(), size.getDescription(), coveredIds.contains(size.getId())))
                .toList();
    }
}

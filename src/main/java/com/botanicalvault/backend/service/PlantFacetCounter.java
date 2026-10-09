package com.botanicalvault.backend.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.botanicalvault.backend.constants.LightExposure;
import com.botanicalvault.backend.dto.response.FacetCountResponseDTO;
import com.botanicalvault.backend.dto.response.FacetResponseDTO;
import com.botanicalvault.backend.dto.response.LightExposureCountResponseDTO;
import com.botanicalvault.backend.dto.response.LightExposureFacetResponseDTO;
import com.botanicalvault.backend.dto.response.OriginFacetResponseDTO;
import com.botanicalvault.backend.dto.response.PlantFacetsResponseDTO;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.entity.Plant;

public final class PlantFacetCounter {

    private PlantFacetCounter() {
    }

    public static PlantFacetsResponseDTO count(List<Plant> plants, PlantSearchCriteria criteria) {
        return new PlantFacetsResponseDTO(
                facet(matching(plants, criteria.withoutType()), plant -> Stream.of(plant.getType().getId())),
                originFacet(matching(plants, criteria.withoutOrigin())),
                facet(matching(plants, criteria.withoutSize()), plant -> Stream.of(plant.getSize().getId())),
                facet(matching(plants, criteria.withoutView()), PlantFacetCounter::viewIdsOf),
                lightExposureFacet(matching(plants, criteria.withoutLightExposure())));
    }

    private static List<Plant> matching(List<Plant> plants, PlantSearchCriteria criteria) {
        return plants.stream().filter(criteria::matches).toList();
    }

    private static FacetResponseDTO facet(List<Plant> plants, Function<Plant, Stream<Long>> idsOf) {
        return new FacetResponseDTO(plants.size(), countsOf(plants, idsOf));
    }

    private static OriginFacetResponseDTO originFacet(List<Plant> plants) {
        long unclassified = plants.stream().filter(plant -> plant.getOrigins().isEmpty()).count();
        return new OriginFacetResponseDTO(plants.size(), unclassified, countsOf(plants, PlantFacetCounter::originIdsOf));
    }

    private static LightExposureFacetResponseDTO lightExposureFacet(List<Plant> plants) {
        Map<LightExposure, Long> counts = plants.stream()
                .flatMap(plant -> plant.getLightExposures().stream())
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
        List<LightExposureCountResponseDTO> options = counts.entrySet().stream()
                .map(entry -> new LightExposureCountResponseDTO(entry.getKey(), entry.getValue()))
                .toList();
        return new LightExposureFacetResponseDTO(plants.size(), options);
    }

    private static List<FacetCountResponseDTO> countsOf(List<Plant> plants, Function<Plant, Stream<Long>> idsOf) {
        Map<Long, Long> counts = plants.stream()
                .flatMap(plant -> idsOf.apply(plant).distinct())
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
        return counts.entrySet().stream()
                .map(entry -> new FacetCountResponseDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static Stream<Long> originIdsOf(Plant plant) {
        return plant.getOrigins().stream().map(Origin::getId);
    }

    private static Stream<Long> viewIdsOf(Plant plant) {
        return plant.getImages().stream().map(image -> image.getView().getId());
    }
}

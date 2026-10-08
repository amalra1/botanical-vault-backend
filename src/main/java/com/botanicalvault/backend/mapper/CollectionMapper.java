package com.botanicalvault.backend.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import com.botanicalvault.backend.dto.response.CollectionImageResponseDTO;
import com.botanicalvault.backend.dto.response.CollectionResponseDTO;
import com.botanicalvault.backend.dto.response.CollectionSummaryResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSummaryResponseDTO;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantCollection;
import com.botanicalvault.backend.entity.PlantImage;
import com.botanicalvault.backend.entity.Size;
import com.botanicalvault.backend.service.CollectionPlantOrder;
import com.botanicalvault.backend.service.SizeCoverage;

public class CollectionMapper {
    private static final int MOSAIC_SIZE = 4;

    public static CollectionSummaryResponseDTO toSummary(PlantCollection collection, List<Size> sizes,
            UnaryOperator<String> urlForKey) {
        List<Plant> plants = CollectionPlantOrder.staircase(collection.getPlants());
        return new CollectionSummaryResponseDTO(
                collection.getId(),
                collection.getName(),
                collection.isFavorites(),
                plants.size(),
                mosaicUrlsOf(plants, urlForKey),
                SizeCoverage.of(sizes, plants),
                collection.getUpdatedAt());
    }

    public static CollectionResponseDTO toResponse(PlantCollection collection, List<Size> sizes,
            UnaryOperator<String> urlForKey, Set<Long> favoritePlantIds) {
        List<Plant> plants = CollectionPlantOrder.staircase(collection.getPlants());
        List<PlantSummaryResponseDTO> summaries = plants.stream()
                .map(plant -> PlantMapper.toSummary(plant, urlForKey, favoritePlantIds.contains(plant.getId())))
                .toList();
        return new CollectionResponseDTO(
                collection.getId(),
                collection.getName(),
                collection.isFavorites(),
                summaries,
                SizeCoverage.of(sizes, plants),
                collection.getUpdatedAt());
    }

    public static List<CollectionImageResponseDTO> toImages(PlantCollection collection,
            UnaryOperator<String> urlForKey) {
        return CollectionPlantOrder.staircase(collection.getPlants()).stream()
                .flatMap(plant -> plant.getImages().stream()
                        .sorted(Comparator.comparing(PlantImage::getPosition))
                        .map(image -> new CollectionImageResponseDTO(
                                plant.getId(),
                                plant.getCommonName(),
                                image.getId(),
                                image.getOriginalFilename(),
                                urlForKey.apply(image.getStorageKey()))))
                .toList();
    }

    private static List<String> mosaicUrlsOf(List<Plant> plants, UnaryOperator<String> urlForKey) {
        return plants.stream()
                .map(plant -> PlantMapper.coverUrlOf(plant, urlForKey))
                .filter(Objects::nonNull)
                .limit(MOSAIC_SIZE)
                .toList();
    }
}

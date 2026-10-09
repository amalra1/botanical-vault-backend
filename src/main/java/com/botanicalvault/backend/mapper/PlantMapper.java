package com.botanicalvault.backend.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

import com.botanicalvault.backend.constants.LightExposure;
import com.botanicalvault.backend.dto.response.PlantImageResponseDTO;
import com.botanicalvault.backend.dto.response.PlantResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSummaryResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantImage;

public class PlantMapper {
    public static PlantResponseDTO toResponse(Plant plant, UnaryOperator<String> urlForKey, boolean favorite,
            List<Long> collectionIds) {
        return new PlantResponseDTO(
                plant.getId(),
                plant.getCommonName(),
                plant.getScientificName(),
                TaxonomyMapper.toItemResponse(plant.getType()),
                TaxonomyMapper.toSizeResponse(plant.getSize()),
                originsOf(plant),
                lightExposuresOf(plant),
                plant.getNotes(),
                imagesOf(plant, urlForKey),
                plant.getCreatedAt(),
                plant.getUpdatedAt(),
                favorite,
                collectionIds);
    }

    public static PlantSummaryResponseDTO toSummary(Plant plant, UnaryOperator<String> urlForKey, boolean favorite) {
        return new PlantSummaryResponseDTO(
                plant.getId(),
                plant.getCommonName(),
                plant.getScientificName(),
                TaxonomyMapper.toItemResponse(plant.getType()),
                TaxonomyMapper.toSizeResponse(plant.getSize()),
                originsOf(plant),
                lightExposuresOf(plant),
                plant.getImages().size(),
                coverUrlOf(plant, urlForKey),
                favorite);
    }

    public static String coverUrlOf(Plant plant, UnaryOperator<String> urlForKey) {
        return plant.getImages().stream()
                .min(Comparator.comparing(PlantImage::getPosition))
                .map(image -> urlForKey.apply(image.getStorageKey()))
                .orElse(null);
    }

    private static List<TaxonomyItemResponseDTO> originsOf(Plant plant) {
        return plant.getOrigins().stream()
                .sorted(Comparator.comparing(Origin::getPosition))
                .map(TaxonomyMapper::toItemResponse)
                .toList();
    }

    private static List<LightExposure> lightExposuresOf(Plant plant) {
        return plant.getLightExposures().stream().sorted().toList();
    }

    private static List<PlantImageResponseDTO> imagesOf(Plant plant, UnaryOperator<String> urlForKey) {
        return plant.getImages().stream()
                .sorted(Comparator.comparing(PlantImage::getPosition))
                .map(image -> new PlantImageResponseDTO(
                        image.getId(),
                        TaxonomyMapper.toItemResponse(image.getView()),
                        image.getOriginalFilename(),
                        urlForKey.apply(image.getStorageKey())))
                .toList();
    }
}

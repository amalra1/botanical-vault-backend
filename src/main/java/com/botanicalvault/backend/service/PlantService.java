package com.botanicalvault.backend.service;

import java.util.List;

import com.botanicalvault.backend.dto.request.PlantCreateRequestDTO;
import com.botanicalvault.backend.dto.request.PlantImageRequestDTO;
import com.botanicalvault.backend.dto.request.PlantImageUpdateRequestDTO;
import com.botanicalvault.backend.dto.request.PlantSearchRequestDTO;
import com.botanicalvault.backend.dto.request.PlantUpdateRequestDTO;
import com.botanicalvault.backend.dto.response.PlantResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSearchResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSummaryResponseDTO;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantImage;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.mapper.PlantMapper;
import com.botanicalvault.backend.repository.PlantRepository;
import com.botanicalvault.backend.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PlantService {
    private final PlantRepository plantRepository;
    private final PlantFields plantFields;
    private final PlantImageSync imageSync;
    private final ImageStorage imageStorage;
    private final StoredImageCleanup storedImageCleanup;

    @Transactional
    public PlantResponseDTO create(PlantCreateRequestDTO dto, List<MultipartFile> files) {
        Plant plant = Plant.builder().build();
        plantFields.apply(plant, dto);
        imageSync.apply(plant, newImages(dto.images()), files);
        return PlantMapper.toResponse(plantRepository.save(plant), imageStorage::readUrl);
    }

    @Transactional
    public PlantResponseDTO update(Long id, PlantUpdateRequestDTO dto, List<MultipartFile> files) {
        Plant plant = findPlant(id);
        plantFields.apply(plant, dto);
        List<String> removedKeys = imageSync.apply(plant, dto.images(), files);
        storedImageCleanup.deleteAfterCommit(removedKeys);
        return PlantMapper.toResponse(plantRepository.saveAndFlush(plant), imageStorage::readUrl);
    }

    @Transactional
    public void delete(Long id) {
        Plant plant = findPlant(id);
        List<String> storageKeys = plant.getImages().stream().map(PlantImage::getStorageKey).toList();
        plantRepository.delete(plant);
        storedImageCleanup.deleteAfterCommit(storageKeys);
    }

    @Transactional(readOnly = true)
    public PlantResponseDTO findById(Long id) {
        return PlantMapper.toResponse(findPlant(id), imageStorage::readUrl);
    }

    @Transactional(readOnly = true)
    public PlantSearchResponseDTO search(PlantSearchRequestDTO dto) {
        PlantSearchCriteria criteria = PlantSearchCriteria.from(dto);
        List<Plant> plants = plantRepository.findAllDetailed();
        List<Plant> matching = plants.stream().filter(criteria::matches).toList();
        List<PlantSummaryResponseDTO> summaries = PlantSorter.sort(matching, dto.sort()).stream()
                .map(plant -> PlantMapper.toSummary(plant, imageStorage::readUrl))
                .toList();
        return new PlantSearchResponseDTO(summaries, PlantFacetCounter.count(plants, criteria));
    }

    private Plant findPlant(Long id) {
        return plantRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", id));
    }

    private static List<PlantImageUpdateRequestDTO> newImages(List<PlantImageRequestDTO> images) {
        return images.stream().map(image -> PlantImageUpdateRequestDTO.newImage(image.viewId())).toList();
    }
}

package com.botanicalvault.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.botanicalvault.backend.dto.request.PlantCreateRequestDTO;
import com.botanicalvault.backend.dto.request.PlantSearchRequestDTO;
import com.botanicalvault.backend.dto.response.PlantResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSearchResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSummaryResponseDTO;
import com.botanicalvault.backend.entity.ImageView;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantImage;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.mapper.PlantMapper;
import com.botanicalvault.backend.repository.PlantRepository;
import com.botanicalvault.backend.storage.ImageStorage;
import com.botanicalvault.backend.util.StorageKeys;
import com.botanicalvault.backend.util.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PlantService {
    private static final int FIRST_POSITION = 1;

    private final PlantRepository plantRepository;
    private final PlantReferenceResolver references;
    private final PlantImageUploads imageUploads;
    private final ImageStorage imageStorage;
    private final StoredImageCleanup storedImageCleanup;

    @Transactional
    public PlantResponseDTO create(PlantCreateRequestDTO dto, List<MultipartFile> files) {
        List<PendingImage> pendingImages = imageUploads.read(dto.images(), files);
        Map<Long, ImageView> views = references.views(pendingImages.stream().map(PendingImage::viewId).toList());
        Plant plant = Plant.builder()
                .commonName(dto.commonName().trim())
                .scientificName(Texts.trimToNull(dto.scientificName()))
                .type(references.type(dto.typeId()))
                .size(references.size(dto.sizeId()))
                .origins(references.origins(dto.originIds()))
                .notes(Texts.trimToNull(dto.notes()))
                .build();
        storeImages(plant, pendingImages, views);
        return PlantMapper.toResponse(plantRepository.save(plant), imageStorage::readUrl);
    }

    @Transactional(readOnly = true)
    public PlantResponseDTO findById(Long id) {
        Plant plant = plantRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", id));
        return PlantMapper.toResponse(plant, imageStorage::readUrl);
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

    private void storeImages(Plant plant, List<PendingImage> pendingImages, Map<Long, ImageView> views) {
        List<String> storedKeys = new ArrayList<>();
        storedImageCleanup.deleteOnRollback(storedKeys);
        for (int index = 0; index < pendingImages.size(); index++) {
            PendingImage pending = pendingImages.get(index);
            String key = StorageKeys.newPngKey();
            imageStorage.save(key, pending.content());
            storedKeys.add(key);
            plant.addImage(PlantImage.builder()
                    .view(views.get(pending.viewId()))
                    .storageKey(key)
                    .originalFilename(pending.originalFilename())
                    .position(FIRST_POSITION + index)
                    .build());
        }
    }
}

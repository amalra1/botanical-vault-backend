package com.botanicalvault.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.PlantImageUpdateRequestDTO;
import com.botanicalvault.backend.entity.ImageView;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantImage;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.storage.ImageStorage;
import com.botanicalvault.backend.util.StorageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class PlantImageSync {
    private static final int FIRST_POSITION = 1;

    private final PlantReferenceResolver references;
    private final PlantImageUploads imageUploads;
    private final ImageStorage imageStorage;
    private final StoredImageCleanup storedImageCleanup;

    public List<String> apply(Plant plant, List<PlantImageUpdateRequestDTO> images, List<MultipartFile> files) {
        Map<Long, PlantImage> existing = plant.getImages().stream()
                .collect(Collectors.toMap(PlantImage::getId, Function.identity()));
        Set<Long> keptIds = keptIds(images, existing);
        List<PendingImage> pendingImages = imageUploads.read(newImageViewIds(images), files);
        Map<Long, ImageView> views = references.views(images.stream().map(PlantImageUpdateRequestDTO::viewId).toList());
        List<String> removedKeys = removeMissing(plant, keptIds);
        Iterator<PendingImage> pending = pendingImages.iterator();
        List<String> storedKeys = new ArrayList<>();
        storedImageCleanup.deleteOnRollback(storedKeys);
        for (int index = 0; index < images.size(); index++) {
            PlantImageUpdateRequestDTO slot = images.get(index);
            ImageView view = views.get(slot.viewId());
            int position = FIRST_POSITION + index;
            if (slot.isNew()) {
                plant.addImage(store(pending.next(), view, position, storedKeys));
            } else {
                PlantImage image = existing.get(slot.id());
                image.setView(view);
                image.setPosition(position);
            }
        }
        return removedKeys;
    }

    private static Set<Long> keptIds(List<PlantImageUpdateRequestDTO> images, Map<Long, PlantImage> existing) {
        Set<Long> keptIds = new HashSet<>();
        images.stream().filter(image -> !image.isNew()).forEach(image -> {
            if (!existing.containsKey(image.id())) {
                throw new ResourceNotFoundException("Plant image", image.id());
            }
            if (!keptIds.add(image.id())) {
                throw new BusinessException(ErrorCode.DUPLICATE_IMAGE, "Image listed more than once: " + image.id());
            }
        });
        return keptIds;
    }

    private static List<Long> newImageViewIds(List<PlantImageUpdateRequestDTO> images) {
        return images.stream().filter(PlantImageUpdateRequestDTO::isNew).map(PlantImageUpdateRequestDTO::viewId).toList();
    }

    private static List<String> removeMissing(Plant plant, Set<Long> keptIds) {
        List<PlantImage> removed = plant.getImages().stream()
                .filter(image -> !keptIds.contains(image.getId()))
                .toList();
        plant.getImages().removeAll(removed);
        return removed.stream().map(PlantImage::getStorageKey).toList();
    }

    private PlantImage store(PendingImage pending, ImageView view, int position, List<String> storedKeys) {
        String key = StorageKeys.newPngKey();
        imageStorage.save(key, pending.content());
        storedKeys.add(key);
        return PlantImage.builder()
                .view(view)
                .storageKey(key)
                .originalFilename(pending.originalFilename())
                .position(position)
                .build();
    }
}

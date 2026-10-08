package com.botanicalvault.backend.service;

import java.time.Clock;
import java.time.Instant;

import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantCollection;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.repository.PlantCollectionRepository;
import com.botanicalvault.backend.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionMembershipService {
    private final PlantCollectionRepository collectionRepository;
    private final PlantRepository plantRepository;
    private final Clock clock;

    @Transactional
    public void addPlant(Long collectionId, Long plantId) {
        add(findCollection(collectionId), plantId);
    }

    @Transactional
    public void removePlant(Long collectionId, Long plantId) {
        remove(findCollection(collectionId), plantId);
    }

    @Transactional
    public void favorite(Long plantId) {
        add(findFavorites(), plantId);
    }

    @Transactional
    public void unfavorite(Long plantId) {
        remove(findFavorites(), plantId);
    }

    private void add(PlantCollection collection, Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", plantId));
        if (collection.addPlant(plant)) {
            collection.markUpdated(Instant.now(clock));
        }
    }

    private void remove(PlantCollection collection, Long plantId) {
        if (!plantRepository.existsById(plantId)) {
            throw new ResourceNotFoundException("Plant", plantId);
        }
        if (collection.removePlant(plantId)) {
            collection.markUpdated(Instant.now(clock));
        }
    }

    private PlantCollection findCollection(Long id) {
        return collectionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Collection", id));
    }

    private PlantCollection findFavorites() {
        return collectionRepository.findByFavoritesTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "favorites"));
    }
}

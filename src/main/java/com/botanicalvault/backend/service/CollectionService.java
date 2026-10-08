package com.botanicalvault.backend.service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.CollectionNameRequestDTO;
import com.botanicalvault.backend.dto.response.CollectionImageResponseDTO;
import com.botanicalvault.backend.dto.response.CollectionResponseDTO;
import com.botanicalvault.backend.dto.response.CollectionSummaryResponseDTO;
import com.botanicalvault.backend.entity.PlantCollection;
import com.botanicalvault.backend.entity.Size;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.mapper.CollectionMapper;
import com.botanicalvault.backend.repository.PlantCollectionRepository;
import com.botanicalvault.backend.repository.SizeRepository;
import com.botanicalvault.backend.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionService {
    private final PlantCollectionRepository collectionRepository;
    private final SizeRepository sizeRepository;
    private final ImageStorage imageStorage;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<CollectionSummaryResponseDTO> findAll() {
        List<Size> sizes = sizeRepository.findAllByOrderByPositionAsc();
        return collectionRepository.findAllDetailed().stream()
                .map(collection -> CollectionMapper.toSummary(collection, sizes, imageStorage::readUrl))
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectionResponseDTO findById(Long id) {
        return toResponse(findCollection(id));
    }

    @Transactional(readOnly = true)
    public List<CollectionImageResponseDTO> findImages(Long id) {
        return CollectionMapper.toImages(findCollection(id), imageStorage::readUrl);
    }

    @Transactional
    public CollectionResponseDTO create(CollectionNameRequestDTO dto) {
        String name = dto.name().trim();
        if (collectionRepository.existsByNameIgnoreCase(name)) {
            throw duplicateName(name);
        }
        Instant now = Instant.now(clock);
        PlantCollection collection = PlantCollection.builder().name(name).createdAt(now).updatedAt(now).build();
        return toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public CollectionResponseDTO rename(Long id, CollectionNameRequestDTO dto) {
        PlantCollection collection = findEditableCollection(id);
        String name = dto.name().trim();
        if (collectionRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicateName(name);
        }
        collection.setName(name);
        collection.markUpdated(Instant.now(clock));
        return toResponse(collectionRepository.saveAndFlush(collection));
    }

    @Transactional
    public void delete(Long id) {
        collectionRepository.delete(findEditableCollection(id));
    }

    private CollectionResponseDTO toResponse(PlantCollection collection) {
        Set<Long> favoritePlantIds = new HashSet<>(collectionRepository.findFavoritePlantIds());
        return CollectionMapper.toResponse(collection, sizeRepository.findAllByOrderByPositionAsc(),
                imageStorage::readUrl, favoritePlantIds);
    }

    private PlantCollection findEditableCollection(Long id) {
        PlantCollection collection = findCollection(id);
        if (collection.isFavorites()) {
            throw new BusinessException(ErrorCode.FAVORITES_LOCKED, "Favorites cannot be renamed or deleted");
        }
        return collection;
    }

    private PlantCollection findCollection(Long id) {
        return collectionRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", id));
    }

    private static BusinessException duplicateName(String name) {
        return new BusinessException(ErrorCode.DUPLICATE_NAME, "Collection already exists: " + name);
    }
}

package com.botanicalvault.backend.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.botanicalvault.backend.entity.ImageView;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.entity.PlantType;
import com.botanicalvault.backend.entity.Size;
import com.botanicalvault.backend.entity.TaxonomyItem;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.repository.ImageViewRepository;
import com.botanicalvault.backend.repository.OriginRepository;
import com.botanicalvault.backend.repository.PlantTypeRepository;
import com.botanicalvault.backend.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlantReferenceResolver {
    private final PlantTypeRepository plantTypeRepository;
    private final SizeRepository sizeRepository;
    private final OriginRepository originRepository;
    private final ImageViewRepository imageViewRepository;

    public PlantType type(Long id) {
        return plantTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Plant type", id));
    }

    public Size size(Long id) {
        return sizeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Size", id));
    }

    public Set<Origin> origins(Collection<Long> ids) {
        Map<Long, Origin> found = byId(originRepository.findAllById(Set.copyOf(ids)));
        return ids.stream()
                .map(id -> require(found, id, "Origin"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<Long, ImageView> views(Collection<Long> ids) {
        Map<Long, ImageView> found = byId(imageViewRepository.findAllById(Set.copyOf(ids)));
        ids.forEach(id -> require(found, id, "Image view"));
        return found;
    }

    private static <T extends TaxonomyItem> Map<Long, T> byId(Iterable<T> items) {
        Map<Long, T> result = new HashMap<>();
        items.forEach(item -> result.put(item.getId(), item));
        return result;
    }

    private static <T> T require(Map<Long, T> found, Long id, String resource) {
        T item = found.get(id);
        if (item == null) {
            throw new ResourceNotFoundException(resource, id);
        }
        return item;
    }
}

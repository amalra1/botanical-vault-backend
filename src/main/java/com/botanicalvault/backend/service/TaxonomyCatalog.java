package com.botanicalvault.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.constants.Taxonomy;
import com.botanicalvault.backend.entity.TaxonomyItem;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.repository.TaxonomyRepository;
import com.botanicalvault.backend.repository.UsageCount;
import org.springframework.stereotype.Component;

@Component
public class TaxonomyCatalog {
    private static final long UNUSED = 0;

    public Map<Long, Long> usageById(TaxonomyRepository<?> repository) {
        return repository.countUsage().stream()
                .collect(Collectors.toMap(UsageCount::getId, UsageCount::getCount));
    }

    public static long usageOf(Map<Long, Long> usage, Long id) {
        return usage.getOrDefault(id, UNUSED);
    }

    public <T extends TaxonomyItem> T create(TaxonomyRepository<T> repository, Taxonomy taxonomy, T item) {
        String name = item.getName().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw duplicate(taxonomy, name);
        }
        item.setName(name);
        item.setPosition(repository.findMaxPosition() + 1);
        return repository.save(item);
    }

    public <T extends TaxonomyItem> T rename(TaxonomyRepository<T> repository, Taxonomy taxonomy, Long id,
            String newName) {
        T item = findOrThrow(repository, taxonomy, id);
        String name = newName.trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicate(taxonomy, name);
        }
        item.setName(name);
        return item;
    }

    public <T extends TaxonomyItem> void reorder(TaxonomyRepository<T> repository, List<Long> ids) {
        List<T> items = repository.findAllByOrderByPositionAsc();
        Map<Long, Integer> positions = TaxonomyOrder.positions(items.stream().map(TaxonomyItem::getId).toList(), ids);
        items.forEach(item -> item.setPosition(positions.get(item.getId())));
    }

    public <T extends TaxonomyItem> void delete(TaxonomyRepository<T> repository, Taxonomy taxonomy, Long id,
            Long reassignTo) {
        findOrThrow(repository, taxonomy, id);
        long usage = usageOf(usageById(repository), id);
        TaxonomyDeletionRules.validate(taxonomy, usage, repository.count(), reassignTo != null);
        if (reassignTo != null) {
            ensureTarget(repository, taxonomy, id, reassignTo);
            repository.reassignUsages(id, reassignTo);
        }
        repository.deleteById(id);
    }

    private static <T extends TaxonomyItem> T findOrThrow(TaxonomyRepository<T> repository, Taxonomy taxonomy,
            Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(taxonomy.resourceName(), id));
    }

    private static void ensureTarget(TaxonomyRepository<?> repository, Taxonomy taxonomy, Long id, Long targetId) {
        if (targetId.equals(id)) {
            throw new BusinessException(ErrorCode.INVALID_REASSIGNMENT,
                    taxonomy.resourceName() + " cannot be reassigned to itself: " + id);
        }
        if (!repository.existsById(targetId)) {
            throw new ResourceNotFoundException(taxonomy.resourceName(), targetId);
        }
    }

    private static BusinessException duplicate(Taxonomy taxonomy, String name) {
        return new BusinessException(ErrorCode.DUPLICATE_NAME, taxonomy.resourceName() + " already exists: " + name);
    }
}

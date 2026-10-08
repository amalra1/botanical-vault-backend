package com.botanicalvault.backend.service;

import java.util.List;
import java.util.Map;

import com.botanicalvault.backend.constants.Taxonomy;
import com.botanicalvault.backend.dto.request.TaxonomyNameRequestDTO;
import com.botanicalvault.backend.dto.request.TaxonomyOrderRequestDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyListItemResponseDTO;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.mapper.TaxonomyMapper;
import com.botanicalvault.backend.repository.OriginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OriginService {
    private static final Taxonomy TAXONOMY = Taxonomy.ORIGIN;
    private final OriginRepository originRepository;
    private final TaxonomyCatalog taxonomyCatalog;

    @Transactional(readOnly = true)
    public List<TaxonomyListItemResponseDTO> findAll() {
        Map<Long, Long> usage = taxonomyCatalog.usageById(originRepository);
        return originRepository.findAllByOrderByPositionAsc().stream()
                .map(item -> TaxonomyMapper.toListItemResponse(item, TaxonomyCatalog.usageOf(usage, item.getId())))
                .toList();
    }

    @Transactional
    public TaxonomyItemResponseDTO create(TaxonomyNameRequestDTO dto) {
        Origin item = Origin.builder().name(dto.name()).build();
        return TaxonomyMapper.toItemResponse(taxonomyCatalog.create(originRepository, TAXONOMY, item));
    }

    @Transactional
    public TaxonomyItemResponseDTO update(Long id, TaxonomyNameRequestDTO dto) {
        return TaxonomyMapper.toItemResponse(taxonomyCatalog.rename(originRepository, TAXONOMY, id, dto.name()));
    }

    @Transactional
    public void reorder(TaxonomyOrderRequestDTO dto) {
        taxonomyCatalog.reorder(originRepository, dto.ids());
    }

    @Transactional
    public void delete(Long id, Long reassignTo) {
        taxonomyCatalog.delete(originRepository, TAXONOMY, id, reassignTo);
    }
}

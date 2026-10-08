package com.botanicalvault.backend.service;

import java.util.List;
import java.util.Map;

import com.botanicalvault.backend.constants.Taxonomy;
import com.botanicalvault.backend.dto.request.TaxonomyNameRequestDTO;
import com.botanicalvault.backend.dto.request.TaxonomyOrderRequestDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyListItemResponseDTO;
import com.botanicalvault.backend.entity.ImageView;
import com.botanicalvault.backend.mapper.TaxonomyMapper;
import com.botanicalvault.backend.repository.ImageViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageViewService {
    private static final Taxonomy TAXONOMY = Taxonomy.IMAGE_VIEW;
    private final ImageViewRepository imageViewRepository;
    private final TaxonomyCatalog taxonomyCatalog;

    @Transactional(readOnly = true)
    public List<TaxonomyListItemResponseDTO> findAll() {
        Map<Long, Long> usage = taxonomyCatalog.usageById(imageViewRepository);
        return imageViewRepository.findAllByOrderByPositionAsc().stream()
                .map(item -> TaxonomyMapper.toListItemResponse(item, TaxonomyCatalog.usageOf(usage, item.getId())))
                .toList();
    }

    @Transactional
    public TaxonomyItemResponseDTO create(TaxonomyNameRequestDTO dto) {
        ImageView item = ImageView.builder().name(dto.name()).build();
        return TaxonomyMapper.toItemResponse(taxonomyCatalog.create(imageViewRepository, TAXONOMY, item));
    }

    @Transactional
    public TaxonomyItemResponseDTO update(Long id, TaxonomyNameRequestDTO dto) {
        return TaxonomyMapper.toItemResponse(taxonomyCatalog.rename(imageViewRepository, TAXONOMY, id, dto.name()));
    }

    @Transactional
    public void reorder(TaxonomyOrderRequestDTO dto) {
        taxonomyCatalog.reorder(imageViewRepository, dto.ids());
    }

    @Transactional
    public void delete(Long id, Long reassignTo) {
        taxonomyCatalog.delete(imageViewRepository, TAXONOMY, id, reassignTo);
    }
}

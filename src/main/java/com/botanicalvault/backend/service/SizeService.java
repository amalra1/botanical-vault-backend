package com.botanicalvault.backend.service;

import java.util.List;
import java.util.Map;

import com.botanicalvault.backend.constants.Taxonomy;
import com.botanicalvault.backend.dto.request.SizeRequestDTO;
import com.botanicalvault.backend.dto.request.TaxonomyOrderRequestDTO;
import com.botanicalvault.backend.dto.response.SizeListItemResponseDTO;
import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.entity.Size;
import com.botanicalvault.backend.mapper.TaxonomyMapper;
import com.botanicalvault.backend.repository.SizeRepository;
import com.botanicalvault.backend.util.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SizeService {
    private static final Taxonomy TAXONOMY = Taxonomy.SIZE;
    private final SizeRepository sizeRepository;
    private final TaxonomyCatalog taxonomyCatalog;

    @Transactional(readOnly = true)
    public List<SizeListItemResponseDTO> findAll() {
        Map<Long, Long> usage = taxonomyCatalog.usageById(sizeRepository);
        return sizeRepository.findAllByOrderByPositionAsc().stream()
                .map(size -> TaxonomyMapper.toSizeListItemResponse(size, TaxonomyCatalog.usageOf(usage, size.getId())))
                .toList();
    }

    @Transactional
    public SizeResponseDTO create(SizeRequestDTO dto) {
        Size size = Size.builder().name(dto.name()).description(Texts.trimToNull(dto.description())).build();
        return TaxonomyMapper.toSizeResponse(taxonomyCatalog.create(sizeRepository, TAXONOMY, size));
    }

    @Transactional
    public SizeResponseDTO update(Long id, SizeRequestDTO dto) {
        Size size = taxonomyCatalog.rename(sizeRepository, TAXONOMY, id, dto.name());
        size.setDescription(Texts.trimToNull(dto.description()));
        return TaxonomyMapper.toSizeResponse(size);
    }

    @Transactional
    public void reorder(TaxonomyOrderRequestDTO dto) {
        taxonomyCatalog.reorder(sizeRepository, dto.ids());
    }

    @Transactional
    public void delete(Long id, Long reassignTo) {
        taxonomyCatalog.delete(sizeRepository, TAXONOMY, id, reassignTo);
    }
}

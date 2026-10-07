package com.botanicalvault.backend.service;

import java.util.List;

import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.mapper.TaxonomyMapper;
import com.botanicalvault.backend.repository.OriginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OriginService {
    private final OriginRepository originRepository;

    @Transactional(readOnly = true)
    public List<TaxonomyItemResponseDTO> findAll() {
        return originRepository.findAllByOrderByPositionAsc().stream()
                .map(TaxonomyMapper::toItemResponse)
                .toList();
    }
}

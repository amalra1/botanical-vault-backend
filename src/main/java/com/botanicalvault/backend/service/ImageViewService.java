package com.botanicalvault.backend.service;

import java.util.List;

import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.mapper.TaxonomyMapper;
import com.botanicalvault.backend.repository.ImageViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageViewService {
    private final ImageViewRepository imageViewRepository;

    @Transactional(readOnly = true)
    public List<TaxonomyItemResponseDTO> findAll() {
        return imageViewRepository.findAllByOrderByPositionAsc().stream()
                .map(TaxonomyMapper::toItemResponse)
                .toList();
    }
}

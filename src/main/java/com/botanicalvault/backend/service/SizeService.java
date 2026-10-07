package com.botanicalvault.backend.service;

import java.util.List;

import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.mapper.TaxonomyMapper;
import com.botanicalvault.backend.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SizeService {
    private final SizeRepository sizeRepository;

    @Transactional(readOnly = true)
    public List<SizeResponseDTO> findAll() {
        return sizeRepository.findAllByOrderByPositionAsc().stream()
                .map(TaxonomyMapper::toSizeResponse)
                .toList();
    }
}

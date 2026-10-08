package com.botanicalvault.backend.service;

import java.util.List;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.OriginCreateRequestDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.exception.BusinessException;
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

    @Transactional
    public TaxonomyItemResponseDTO create(OriginCreateRequestDTO dto) {
        String name = dto.name().trim();
        if (originRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NAME, "Origin already exists: " + name);
        }
        Origin origin = Origin.builder()
                .name(name)
                .position(originRepository.findMaxPosition() + 1)
                .build();
        return TaxonomyMapper.toItemResponse(originRepository.save(origin));
    }
}

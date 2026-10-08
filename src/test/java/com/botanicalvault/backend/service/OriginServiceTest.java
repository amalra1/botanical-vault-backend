package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.OriginCreateRequestDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.repository.OriginRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OriginServiceTest {
    private final OriginRepository originRepository = mock(OriginRepository.class);
    private final OriginService originService = new OriginService(originRepository);

    @Test
    void createsTrimmedOriginAtTheEnd() {
        when(originRepository.findMaxPosition()).thenReturn(2);
        when(originRepository.save(any(Origin.class))).thenAnswer(invocation -> {
            Origin origin = invocation.getArgument(0);
            origin.setId(3L);
            return origin;
        });

        TaxonomyItemResponseDTO response = originService.create(new OriginCreateRequestDTO("  Exótica "));

        ArgumentCaptor<Origin> saved = ArgumentCaptor.forClass(Origin.class);
        verify(originRepository).save(saved.capture());
        assertThat(saved.getValue().getPosition()).isEqualTo(3);
        assertThat(response).isEqualTo(new TaxonomyItemResponseDTO(3L, "Exótica"));
    }

    @Test
    void rejectsDuplicateNameIgnoringCase() {
        when(originRepository.existsByNameIgnoreCase("mata atlântica")).thenReturn(true);

        assertThatThrownBy(() -> originService.create(new OriginCreateRequestDTO("mata atlântica")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_NAME);
        verify(originRepository, never()).save(any());
    }
}

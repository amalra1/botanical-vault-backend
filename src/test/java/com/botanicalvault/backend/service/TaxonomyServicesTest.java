package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.ImageView;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.entity.PlantType;
import com.botanicalvault.backend.entity.Size;
import com.botanicalvault.backend.repository.ImageViewRepository;
import com.botanicalvault.backend.repository.OriginRepository;
import com.botanicalvault.backend.repository.PlantTypeRepository;
import com.botanicalvault.backend.repository.SizeRepository;
import org.junit.jupiter.api.Test;

class TaxonomyServicesTest {
    private final OriginRepository originRepository = mock(OriginRepository.class);
    private final PlantTypeRepository plantTypeRepository = mock(PlantTypeRepository.class);
    private final SizeRepository sizeRepository = mock(SizeRepository.class);
    private final ImageViewRepository imageViewRepository = mock(ImageViewRepository.class);

    @Test
    void originsKeepRepositoryOrder() {
        when(originRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(
                Origin.builder().id(2L).name("Floresta Ombrófila Mista").position(1).build(),
                Origin.builder().id(1L).name("Mata Atlântica").position(2).build()));

        assertThat(new OriginService(originRepository).findAll()).containsExactly(
                new TaxonomyItemResponseDTO(2L, "Floresta Ombrófila Mista"),
                new TaxonomyItemResponseDTO(1L, "Mata Atlântica"));
    }

    @Test
    void plantTypesAreMappedToItems() {
        when(plantTypeRepository.findAllByOrderByPositionAsc())
                .thenReturn(List.of(PlantType.builder().id(1L).name("Forração").position(1).build()));

        assertThat(new PlantTypeService(plantTypeRepository).findAll())
                .containsExactly(new TaxonomyItemResponseDTO(1L, "Forração"));
    }

    @Test
    void imageViewsAreMappedToItems() {
        when(imageViewRepository.findAllByOrderByPositionAsc())
                .thenReturn(List.of(ImageView.builder().id(1L).name("Elevação").position(1).build()));

        assertThat(new ImageViewService(imageViewRepository).findAll())
                .containsExactly(new TaxonomyItemResponseDTO(1L, "Elevação"));
    }

    @Test
    void sizesIncludeDescription() {
        when(sizeRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(
                Size.builder().id(1L).name("Pequeno").description("P · baixa").position(1).build()));

        assertThat(new SizeService(sizeRepository).findAll())
                .containsExactly(new SizeResponseDTO(1L, "Pequeno", "P · baixa"));
    }
}

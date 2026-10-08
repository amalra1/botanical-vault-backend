package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.TestUsage;
import com.botanicalvault.backend.dto.request.SizeRequestDTO;
import com.botanicalvault.backend.dto.response.SizeListItemResponseDTO;
import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyListItemResponseDTO;
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
    private final TaxonomyCatalog taxonomyCatalog = new TaxonomyCatalog();

    @Test
    void originsKeepRepositoryOrderWithUsage() {
        when(originRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(
                Origin.builder().id(2L).name("Floresta Ombrófila Mista").position(1).build(),
                Origin.builder().id(1L).name("Mata Atlântica").position(2).build()));
        when(originRepository.countUsage()).thenReturn(List.of(new TestUsage(1L, 4)));

        assertThat(new OriginService(originRepository, taxonomyCatalog).findAll()).containsExactly(
                new TaxonomyListItemResponseDTO(2L, "Floresta Ombrófila Mista", 0),
                new TaxonomyListItemResponseDTO(1L, "Mata Atlântica", 4));
    }

    @Test
    void plantTypesAreMappedToItems() {
        when(plantTypeRepository.findAllByOrderByPositionAsc())
                .thenReturn(List.of(PlantType.builder().id(1L).name("Forração").position(1).build()));
        when(plantTypeRepository.countUsage()).thenReturn(List.of(new TestUsage(1L, 2)));

        assertThat(new PlantTypeService(plantTypeRepository, taxonomyCatalog).findAll())
                .containsExactly(new TaxonomyListItemResponseDTO(1L, "Forração", 2));
    }

    @Test
    void imageViewsAreMappedToItems() {
        when(imageViewRepository.findAllByOrderByPositionAsc())
                .thenReturn(List.of(ImageView.builder().id(1L).name("Elevação").position(1).build()));

        assertThat(new ImageViewService(imageViewRepository, taxonomyCatalog).findAll())
                .containsExactly(new TaxonomyListItemResponseDTO(1L, "Elevação", 0));
    }

    @Test
    void sizesIncludeDescription() {
        when(sizeRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(
                Size.builder().id(1L).name("Pequeno").description("P · baixa").position(1).build()));

        assertThat(new SizeService(sizeRepository, taxonomyCatalog).findAll())
                .containsExactly(new SizeListItemResponseDTO(1L, "Pequeno", "P · baixa", 0));
    }

    @Test
    void sizeUpdateTrimsBlankDescriptionToNull() {
        Size size = Size.builder().id(1L).name("Pequeno").description("P · baixa").position(1).build();
        when(sizeRepository.findById(1L)).thenReturn(Optional.of(size));

        SizeResponseDTO response = new SizeService(sizeRepository, taxonomyCatalog)
                .update(1L, new SizeRequestDTO(" Mini ", "  "));

        assertThat(response).isEqualTo(new SizeResponseDTO(1L, "Mini", null));
    }
}

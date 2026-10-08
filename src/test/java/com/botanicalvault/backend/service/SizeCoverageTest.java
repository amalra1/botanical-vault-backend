package com.botanicalvault.backend.service;

import static com.botanicalvault.backend.TestPlants.ELEVATION;
import static com.botanicalvault.backend.TestPlants.LARGE;
import static com.botanicalvault.backend.TestPlants.PALM;
import static com.botanicalvault.backend.TestPlants.SMALL;
import static com.botanicalvault.backend.TestPlants.plant;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.botanicalvault.backend.dto.response.SizeCoverageResponseDTO;
import com.botanicalvault.backend.entity.Size;
import org.junit.jupiter.api.Test;

class SizeCoverageTest {
    private static final Size MEDIUM = Size.builder().id(2L).name("Médio").description("M · média").position(2).build();
    private static final List<Size> SIZES = List.of(SMALL, MEDIUM, LARGE);

    @Test
    void marksEverySizeThatHasAPlantInSizeOrder() {
        List<SizeCoverageResponseDTO> coverage = SizeCoverage.of(SIZES, List.of(
                plant(1L, "Jerivá", PALM, LARGE, List.of(), List.of(ELEVATION)),
                plant(2L, "Grama", PALM, SMALL, List.of(), List.of(ELEVATION))));

        assertThat(coverage).extracting(SizeCoverageResponseDTO::description)
                .containsExactly("P · baixa", "M · média", "G · alta");
        assertThat(coverage).extracting(SizeCoverageResponseDTO::covered).containsExactly(true, false, true);
    }

    @Test
    void emptyCollectionCoversNothing() {
        assertThat(SizeCoverage.of(SIZES, List.of()))
                .extracting(SizeCoverageResponseDTO::covered)
                .containsExactly(false, false, false);
    }
}

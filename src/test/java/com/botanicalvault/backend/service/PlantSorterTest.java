package com.botanicalvault.backend.service;

import static com.botanicalvault.backend.TestPlants.ELEVATION;
import static com.botanicalvault.backend.TestPlants.PALM;
import static com.botanicalvault.backend.TestPlants.SMALL;
import static com.botanicalvault.backend.TestPlants.plant;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.botanicalvault.backend.constants.PlantSort;
import com.botanicalvault.backend.entity.Plant;
import org.junit.jupiter.api.Test;

class PlantSorterTest {
    private static final List<Plant> PLANTS = List.of(
            plant(1L, "jerivá", PALM, SMALL, List.of(), List.of(ELEVATION)),
            plant(2L, "Érica", PALM, SMALL, List.of(), List.of(ELEVATION)),
            plant(3L, "Butiá", PALM, SMALL, List.of(), List.of(ELEVATION)));

    @Test
    void sortsByNameIgnoringAccentsAndCase() {
        assertThat(PlantSorter.sort(PLANTS, PlantSort.NAME)).extracting(Plant::getCommonName)
                .containsExactly("Butiá", "Érica", "jerivá");
    }

    @Test
    void sortsNewestFirst() {
        assertThat(PlantSorter.sort(PLANTS, PlantSort.RECENT)).extracting(Plant::getId)
                .containsExactly(3L, 2L, 1L);
    }
}

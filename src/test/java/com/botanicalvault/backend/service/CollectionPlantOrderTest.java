package com.botanicalvault.backend.service;

import static com.botanicalvault.backend.TestPlants.ELEVATION;
import static com.botanicalvault.backend.TestPlants.LARGE;
import static com.botanicalvault.backend.TestPlants.PALM;
import static com.botanicalvault.backend.TestPlants.SHRUB;
import static com.botanicalvault.backend.TestPlants.SMALL;
import static com.botanicalvault.backend.TestPlants.plant;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.botanicalvault.backend.entity.Plant;
import org.junit.jupiter.api.Test;

class CollectionPlantOrderTest {

    @Test
    void ordersFromShortestToTallestThenByName() {
        List<Plant> ordered = CollectionPlantOrder.staircase(List.of(
                plant(1L, "Jerivá", PALM, LARGE, List.of(), List.of(ELEVATION)),
                plant(2L, "Pitangueira", SHRUB, SMALL, List.of(), List.of(ELEVATION)),
                plant(3L, "Araucária", PALM, LARGE, List.of(), List.of(ELEVATION)),
                plant(4L, "Érica", SHRUB, SMALL, List.of(), List.of(ELEVATION))));

        assertThat(ordered).extracting(Plant::getCommonName)
                .containsExactly("Érica", "Pitangueira", "Araucária", "Jerivá");
    }
}

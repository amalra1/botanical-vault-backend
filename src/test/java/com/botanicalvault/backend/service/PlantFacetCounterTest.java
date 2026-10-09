package com.botanicalvault.backend.service;

import static com.botanicalvault.backend.TestPlants.ATLANTIC_FOREST;
import static com.botanicalvault.backend.TestPlants.ELEVATION;
import static com.botanicalvault.backend.TestPlants.LARGE;
import static com.botanicalvault.backend.TestPlants.MIXED_FOREST;
import static com.botanicalvault.backend.TestPlants.PALM;
import static com.botanicalvault.backend.TestPlants.SHRUB;
import static com.botanicalvault.backend.TestPlants.SMALL;
import static com.botanicalvault.backend.TestPlants.TOP;
import static com.botanicalvault.backend.TestPlants.plant;
import static com.botanicalvault.backend.TestPlants.withLight;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.botanicalvault.backend.constants.LightExposure;
import com.botanicalvault.backend.dto.request.PlantSearchRequestDTO;
import com.botanicalvault.backend.dto.response.FacetCountResponseDTO;
import com.botanicalvault.backend.dto.response.LightExposureCountResponseDTO;
import com.botanicalvault.backend.dto.response.PlantFacetsResponseDTO;
import com.botanicalvault.backend.entity.Plant;
import org.junit.jupiter.api.Test;

class PlantFacetCounterTest {
    private static final List<Plant> PLANTS = List.of(
            withLight(plant(1L, "Butiá", PALM, LARGE, List.of(MIXED_FOREST), List.of(ELEVATION, ELEVATION)),
                    LightExposure.FULL_SUN),
            withLight(plant(2L, "Jerivá", PALM, LARGE, List.of(MIXED_FOREST, ATLANTIC_FOREST), List.of(ELEVATION, TOP)),
                    LightExposure.FULL_SUN, LightExposure.PARTIAL_SHADE),
            withLight(plant(3L, "Guaimbê", SHRUB, SMALL, List.of(), List.of(TOP)), LightExposure.SHADE));

    @Test
    void countsEveryOptionWithoutFilters() {
        PlantFacetsResponseDTO facets = count(new PlantSearchRequestDTO(null, null, null, false, null, null, null, null));

        assertThat(facets.types().all()).isEqualTo(3);
        assertThat(facets.types().options()).containsExactly(
                new FacetCountResponseDTO(SHRUB.getId(), 1), new FacetCountResponseDTO(PALM.getId(), 2));
        assertThat(facets.origins().unclassified()).isEqualTo(1);
        assertThat(facets.origins().options()).containsExactly(
                new FacetCountResponseDTO(MIXED_FOREST.getId(), 2), new FacetCountResponseDTO(ATLANTIC_FOREST.getId(), 1));
        assertThat(facets.views().options()).containsExactly(
                new FacetCountResponseDTO(ELEVATION.getId(), 2), new FacetCountResponseDTO(TOP.getId(), 2));
    }

    @Test
    void eachGroupIgnoresItsOwnFilterButAppliesTheOthers() {
        PlantFacetsResponseDTO facets = count(new PlantSearchRequestDTO(null, PALM.getId(), null, false, null, null, null, null));

        assertThat(facets.types().all()).isEqualTo(3);
        assertThat(facets.sizes().all()).isEqualTo(2);
        assertThat(facets.sizes().options()).containsExactly(new FacetCountResponseDTO(LARGE.getId(), 2));
        assertThat(facets.origins().unclassified()).isZero();
    }

    @Test
    void searchNarrowsEveryGroup() {
        PlantFacetsResponseDTO facets = count(new PlantSearchRequestDTO("guaimbe", null, null, false, null, null, null, null));

        assertThat(facets.types().all()).isEqualTo(1);
        assertThat(facets.origins().unclassified()).isEqualTo(1);
        assertThat(facets.origins().options()).isEmpty();
    }

    @Test
    void lightExposureGroupIgnoresItsOwnFilterInScaleOrder() {
        PlantFacetsResponseDTO facets = count(
                new PlantSearchRequestDTO(null, null, null, false, null, null, LightExposure.SHADE, null));

        assertThat(facets.types().all()).isEqualTo(1);
        assertThat(facets.lightExposures().all()).isEqualTo(3);
        assertThat(facets.lightExposures().options()).containsExactly(
                new LightExposureCountResponseDTO(LightExposure.SHADE, 1),
                new LightExposureCountResponseDTO(LightExposure.PARTIAL_SHADE, 1),
                new LightExposureCountResponseDTO(LightExposure.FULL_SUN, 2));
    }

    private static PlantFacetsResponseDTO count(PlantSearchRequestDTO dto) {
        return PlantFacetCounter.count(PLANTS, PlantSearchCriteria.from(dto));
    }
}

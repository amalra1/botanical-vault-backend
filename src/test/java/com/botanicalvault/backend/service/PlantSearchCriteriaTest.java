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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.PlantSearchRequestDTO;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

class PlantSearchCriteriaTest {
    private static final Plant BUTIA = plant(1L, "Butiá", PALM, LARGE, List.of(MIXED_FOREST), List.of(ELEVATION));
    private static final Plant GUAIMBE = plant(2L, "Guaimbê", SHRUB, SMALL, List.of(), List.of(TOP));

    @Test
    void emptyCriteriaMatchesEverything() {
        PlantSearchCriteria criteria = criteria(null, null, null, false, null, null);

        assertThat(criteria.matches(BUTIA)).isTrue();
        assertThat(criteria.matches(GUAIMBE)).isTrue();
    }

    @Test
    void queryIgnoresAccentsAndCase() {
        PlantSearchCriteria criteria = criteria("BUTIA", null, null, false, null, null);

        assertThat(criteria.matches(BUTIA)).isTrue();
        assertThat(criteria.matches(GUAIMBE)).isFalse();
    }

    @Test
    void queryAlsoSearchesScientificName() {
        Plant withScientificName = plant(3L, "Jerivá", PALM, LARGE, List.of(), List.of(ELEVATION));
        withScientificName.setScientificName("Syagrus romanzoffiana");

        assertThat(criteria("syagrus", null, null, false, null, null).matches(withScientificName)).isTrue();
    }

    @Test
    void filtersByTypeSizeOriginAndView() {
        assertThat(criteria(null, PALM.getId(), null, false, null, null).matches(GUAIMBE)).isFalse();
        assertThat(criteria(null, null, null, false, SMALL.getId(), null).matches(GUAIMBE)).isTrue();
        assertThat(criteria(null, null, MIXED_FOREST.getId(), false, null, null).matches(BUTIA)).isTrue();
        assertThat(criteria(null, null, ATLANTIC_FOREST.getId(), false, null, null).matches(BUTIA)).isFalse();
        assertThat(criteria(null, null, null, false, null, TOP.getId()).matches(GUAIMBE)).isTrue();
        assertThat(criteria(null, null, null, false, null, TOP.getId()).matches(BUTIA)).isFalse();
    }

    @Test
    void unclassifiedMatchesOnlyPlantsWithoutOrigin() {
        PlantSearchCriteria criteria = criteria(null, null, null, true, null, null);

        assertThat(criteria.matches(GUAIMBE)).isTrue();
        assertThat(criteria.matches(BUTIA)).isFalse();
    }

    @Test
    void rejectsOriginTogetherWithUnclassified() {
        PlantSearchRequestDTO dto = new PlantSearchRequestDTO(null, null, 1L, true, null, null, null);

        assertThatThrownBy(() -> PlantSearchCriteria.from(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICTING_FILTERS);
    }

    private static PlantSearchCriteria criteria(String q, Long typeId, Long originId, boolean unclassified,
            Long sizeId, Long viewId) {
        return PlantSearchCriteria.from(
                new PlantSearchRequestDTO(q, typeId, originId, unclassified, sizeId, viewId, null));
    }
}

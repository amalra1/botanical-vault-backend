package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.constants.Taxonomy;
import com.botanicalvault.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

class TaxonomyDeletionRulesTest {

    @Test
    void unusedItemNeedsNoTarget() {
        assertThatCode(() -> TaxonomyDeletionRules.validate(Taxonomy.PLANT_TYPE, 0, 4, false))
                .doesNotThrowAnyException();
    }

    @Test
    void usedTypeNeedsTarget() {
        assertError(() -> TaxonomyDeletionRules.validate(Taxonomy.PLANT_TYPE, 3, 4, false),
                ErrorCode.REASSIGNMENT_REQUIRED);
    }

    @Test
    void usedTypeWithTargetIsAllowed() {
        assertThatCode(() -> TaxonomyDeletionRules.validate(Taxonomy.SIZE, 3, 3, true))
                .doesNotThrowAnyException();
    }

    @Test
    void usedOriginCanBeUnassigned() {
        assertThatCode(() -> TaxonomyDeletionRules.validate(Taxonomy.ORIGIN, 5, 2, false))
                .doesNotThrowAnyException();
    }

    @Test
    void lastOriginCanBeDeleted() {
        assertThatCode(() -> TaxonomyDeletionRules.validate(Taxonomy.ORIGIN, 0, 1, false))
                .doesNotThrowAnyException();
    }

    @Test
    void lastViewCannotBeDeleted() {
        assertError(() -> TaxonomyDeletionRules.validate(Taxonomy.IMAGE_VIEW, 0, 1, false),
                ErrorCode.LAST_TAXONOMY_ITEM);
    }

    private static void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
    }
}

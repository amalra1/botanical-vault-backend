package com.botanicalvault.backend.service;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.constants.Taxonomy;
import com.botanicalvault.backend.exception.BusinessException;

public final class TaxonomyDeletionRules {
    private static final int LAST_ITEM_COUNT = 1;

    private TaxonomyDeletionRules() {
    }

    public static void validate(Taxonomy taxonomy, long usageCount, long itemCount, boolean hasTarget) {
        if (!taxonomy.allowsUnassign() && itemCount <= LAST_ITEM_COUNT) {
            throw new BusinessException(ErrorCode.LAST_TAXONOMY_ITEM,
                    taxonomy.resourceName() + " list cannot be empty");
        }
        if (!taxonomy.allowsUnassign() && usageCount > 0 && !hasTarget) {
            throw new BusinessException(ErrorCode.REASSIGNMENT_REQUIRED,
                    taxonomy.resourceName() + " is in use and needs a reassignment target");
        }
    }
}

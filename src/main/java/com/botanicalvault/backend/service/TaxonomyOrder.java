package com.botanicalvault.backend.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.exception.BusinessException;

public final class TaxonomyOrder {
    private static final int FIRST_POSITION = 1;

    private TaxonomyOrder() {
    }

    public static Map<Long, Integer> positions(List<Long> currentIds, List<Long> requestedIds) {
        if (!isPermutation(currentIds, requestedIds)) {
            throw new BusinessException(ErrorCode.INVALID_TAXONOMY_ORDER,
                    "Order must list every item exactly once: " + requestedIds);
        }
        Map<Long, Integer> positions = new LinkedHashMap<>();
        for (int index = 0; index < requestedIds.size(); index++) {
            positions.put(requestedIds.get(index), FIRST_POSITION + index);
        }
        return positions;
    }

    private static boolean isPermutation(List<Long> currentIds, List<Long> requestedIds) {
        return requestedIds.size() == currentIds.size()
                && new HashSet<>(requestedIds).size() == requestedIds.size()
                && new HashSet<>(currentIds).equals(new HashSet<>(requestedIds));
    }
}

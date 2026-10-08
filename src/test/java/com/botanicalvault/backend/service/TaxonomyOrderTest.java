package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TaxonomyOrderTest {
    private static final List<Long> CURRENT = List.of(1L, 2L, 3L);

    @Test
    void numbersPositionsFromOneInRequestedOrder() {
        assertThat(TaxonomyOrder.positions(CURRENT, List.of(3L, 1L, 2L)))
                .containsExactly(entry(3L, 1), entry(1L, 2), entry(2L, 3));
    }

    @ParameterizedTest
    @MethodSource("invalidOrders")
    void rejectsAnythingButAPermutation(List<Long> requested) {
        assertThatThrownBy(() -> TaxonomyOrder.positions(CURRENT, requested))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TAXONOMY_ORDER);
    }

    static List<Arguments> invalidOrders() {
        return List.of(
                Arguments.of(List.of(1L, 2L)),
                Arguments.of(List.of(1L, 2L, 3L, 4L)),
                Arguments.of(List.of(1L, 1L, 2L)),
                Arguments.of(List.of(1L, 2L, 9L)));
    }
}

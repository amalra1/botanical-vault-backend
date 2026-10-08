package com.botanicalvault.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SearchTextsTest {

    @Test
    void normalizeRemovesAccentsAndCase() {
        assertThat(SearchTexts.normalize("  Butiá ÇÃO ")).isEqualTo("butia cao");
    }

    @Test
    void normalizeTurnsBlankIntoNull() {
        assertThat(SearchTexts.normalize("   ")).isNull();
        assertThat(SearchTexts.normalize(null)).isNull();
    }

    @Test
    void containsIgnoresAccentsInText() {
        assertThat(SearchTexts.contains("Guaimbê", "guaimbe")).isTrue();
        assertThat(SearchTexts.contains(null, "guaimbe")).isFalse();
    }
}

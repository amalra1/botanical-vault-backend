package com.botanicalvault.backend.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SearchTexts {
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    private SearchTexts() {
    }

    public static String normalize(String value) {
        String trimmed = Texts.trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        return COMBINING_MARKS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
    }

    public static boolean contains(String text, String normalizedQuery) {
        String normalized = normalize(text);
        return normalized != null && normalized.contains(normalizedQuery);
    }
}

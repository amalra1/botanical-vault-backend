package com.botanicalvault.backend.service;

import java.text.Collator;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.botanicalvault.backend.constants.PlantSort;
import com.botanicalvault.backend.entity.Plant;

public final class PlantSorter {
    private static final Locale PORTUGUESE = Locale.forLanguageTag("pt-BR");

    private PlantSorter() {
    }

    public static List<Plant> sort(List<Plant> plants, PlantSort sort) {
        return plants.stream().sorted(comparatorFor(sort)).toList();
    }

    private static Comparator<Plant> comparatorFor(PlantSort sort) {
        return switch (sort) {
            case NAME -> byName();
            case RECENT -> newestFirst().thenComparing(byName());
        };
    }

    private static Comparator<Plant> newestFirst() {
        return Comparator.comparing(Plant::getCreatedAt, Comparator.nullsLast(Comparator.<Instant>reverseOrder()));
    }

    private static Comparator<Plant> byName() {
        Collator collator = Collator.getInstance(PORTUGUESE);
        collator.setStrength(Collator.SECONDARY);
        return Comparator.comparing(Plant::getCommonName, collator).thenComparing(Plant::getId);
    }
}

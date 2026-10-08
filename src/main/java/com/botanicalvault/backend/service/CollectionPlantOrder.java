package com.botanicalvault.backend.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.botanicalvault.backend.entity.Plant;

public final class CollectionPlantOrder {

    private CollectionPlantOrder() {
    }

    public static List<Plant> staircase(Collection<Plant> plants) {
        Comparator<Plant> bySize = Comparator.comparing(plant -> plant.getSize().getPosition());
        return plants.stream().sorted(bySize.thenComparing(PlantSorter.byName())).toList();
    }
}

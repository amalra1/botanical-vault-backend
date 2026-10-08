package com.botanicalvault.backend;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;

import com.botanicalvault.backend.entity.ImageView;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantImage;
import com.botanicalvault.backend.entity.PlantType;
import com.botanicalvault.backend.entity.Size;

public final class TestPlants {
    public static final PlantType SHRUB = PlantType.builder().id(2L).name("Arbusto").position(2).build();
    public static final PlantType PALM = PlantType.builder().id(4L).name("Palmeira").position(4).build();
    public static final Size SMALL = Size.builder().id(1L).name("Pequeno").description("P · baixa").position(1).build();
    public static final Size LARGE = Size.builder().id(3L).name("Grande").description("G · alta").position(3).build();
    public static final Origin MIXED_FOREST =
            Origin.builder().id(1L).name("Floresta Ombrófila Mista").position(1).build();
    public static final Origin ATLANTIC_FOREST = Origin.builder().id(2L).name("Mata Atlântica").position(2).build();
    public static final ImageView ELEVATION = ImageView.builder().id(1L).name("Elevação").position(1).build();
    public static final ImageView TOP = ImageView.builder().id(2L).name("Vista superior").position(2).build();
    private static final long IMAGE_ID_FACTOR = 100;

    private TestPlants() {
    }

    public static Plant plant(Long id, String commonName, PlantType type, Size size, List<Origin> origins,
            List<ImageView> views) {
        Plant plant = Plant.builder()
                .id(id)
                .commonName(commonName)
                .type(type)
                .size(size)
                .origins(new LinkedHashSet<>(origins))
                .createdAt(Instant.parse("2026-10-01T12:00:00Z").plusSeconds(id))
                .build();
        for (int index = 0; index < views.size(); index++) {
            plant.addImage(PlantImage.builder()
                    .id(id * IMAGE_ID_FACTOR + index)
                    .view(views.get(index))
                    .storageKey(commonName + index + ".png")
                    .originalFilename(commonName + ".png")
                    .position(index + 1)
                    .build());
        }
        return plant;
    }
}

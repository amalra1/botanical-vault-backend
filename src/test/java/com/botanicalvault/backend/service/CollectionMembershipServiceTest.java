package com.botanicalvault.backend.service;

import static com.botanicalvault.backend.TestPlants.ELEVATION;
import static com.botanicalvault.backend.TestPlants.PALM;
import static com.botanicalvault.backend.TestPlants.SMALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.TestPlants;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantCollection;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.repository.PlantCollectionRepository;
import com.botanicalvault.backend.repository.PlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollectionMembershipServiceTest {
    private static final Instant CREATED = Instant.parse("2026-10-01T12:00:00Z");
    private static final Instant NOW = Instant.parse("2026-10-08T12:00:00Z");
    private static final Long COLLECTION_ID = 2L;
    private static final Long PLANT_ID = 7L;

    private final PlantCollectionRepository collectionRepository = mock(PlantCollectionRepository.class);
    private final PlantRepository plantRepository = mock(PlantRepository.class);
    private final CollectionMembershipService membershipService = new CollectionMembershipService(
            collectionRepository, plantRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    private final Plant butia = TestPlants.plant(PLANT_ID, "Butiá", PALM, SMALL, List.of(), List.of(ELEVATION));
    private final PlantCollection batel =
            PlantCollection.builder().id(COLLECTION_ID).name("Batel").updatedAt(CREATED).build();
    private final PlantCollection favorites =
            PlantCollection.builder().id(1L).name("Favoritos").favorites(true).updatedAt(CREATED).build();

    @BeforeEach
    void setUp() {
        when(collectionRepository.findById(COLLECTION_ID)).thenReturn(Optional.of(batel));
        when(collectionRepository.findByFavoritesTrue()).thenReturn(Optional.of(favorites));
        when(plantRepository.findById(PLANT_ID)).thenReturn(Optional.of(butia));
        when(plantRepository.existsById(PLANT_ID)).thenReturn(true);
    }

    @Test
    void addingTwiceKeepsOneEntry() {
        membershipService.addPlant(COLLECTION_ID, PLANT_ID);
        membershipService.addPlant(COLLECTION_ID, PLANT_ID);

        assertThat(batel.getPlants()).containsExactly(butia);
        assertThat(batel.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void removingAbsentPlantChangesNothing() {
        membershipService.removePlant(COLLECTION_ID, PLANT_ID);

        assertThat(batel.getUpdatedAt()).isEqualTo(CREATED);
    }

    @Test
    void favoriteTogglesTheFavoritesCollection() {
        membershipService.favorite(PLANT_ID);
        assertThat(favorites.getPlants()).containsExactly(butia);

        membershipService.unfavorite(PLANT_ID);
        assertThat(favorites.getPlants()).isEmpty();
        assertThat(favorites.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void unknownPlantOrCollectionIsNotFound() {
        when(plantRepository.findById(99L)).thenReturn(Optional.empty());
        when(collectionRepository.findById(98L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.addPlant(COLLECTION_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> membershipService.removePlant(COLLECTION_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> membershipService.addPlant(98L, PLANT_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

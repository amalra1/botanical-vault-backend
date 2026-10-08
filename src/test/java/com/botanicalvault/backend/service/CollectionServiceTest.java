package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.TestPlants;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.CollectionNameRequestDTO;
import com.botanicalvault.backend.dto.response.CollectionResponseDTO;
import com.botanicalvault.backend.entity.PlantCollection;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.repository.PlantCollectionRepository;
import com.botanicalvault.backend.repository.SizeRepository;
import com.botanicalvault.backend.storage.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollectionServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-08T12:00:00Z");
    private static final Long FAVORITES_ID = 1L;
    private static final Long BATEL_ID = 2L;

    private final PlantCollectionRepository collectionRepository = mock(PlantCollectionRepository.class);
    private final SizeRepository sizeRepository = mock(SizeRepository.class);
    private final ImageStorage imageStorage = mock(ImageStorage.class);
    private final CollectionService collectionService = new CollectionService(collectionRepository, sizeRepository,
            imageStorage, Clock.fixed(NOW, ZoneOffset.UTC));
    private final PlantCollection favorites =
            PlantCollection.builder().id(FAVORITES_ID).name("Favoritos").favorites(true).build();
    private final PlantCollection batel = PlantCollection.builder().id(BATEL_ID).name("Batel").build();

    @BeforeEach
    void setUp() {
        when(sizeRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(TestPlants.SMALL, TestPlants.LARGE));
        when(collectionRepository.findDetailedById(FAVORITES_ID)).thenReturn(Optional.of(favorites));
        when(collectionRepository.findDetailedById(BATEL_ID)).thenReturn(Optional.of(batel));
        when(collectionRepository.save(any(PlantCollection.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collectionRepository.saveAndFlush(any(PlantCollection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(imageStorage.readUrl(anyString())).thenAnswer(invocation -> "url:" + invocation.getArgument(0));
    }

    @Test
    void createsTrimmedCollectionStampedWithClock() {
        CollectionResponseDTO response = collectionService.create(new CollectionNameRequestDTO("  Jardim frontal "));

        assertThat(response.name()).isEqualTo("Jardim frontal");
        assertThat(response.favorites()).isFalse();
        assertThat(response.updatedAt()).isEqualTo(NOW);
        assertThat(response.sizeCoverage()).hasSize(2).noneMatch(size -> size.covered());
    }

    @Test
    void rejectsDuplicateNameOnCreateAndRename() {
        when(collectionRepository.existsByNameIgnoreCase("batel")).thenReturn(true);
        when(collectionRepository.existsByNameIgnoreCaseAndIdNot("Favoritos", BATEL_ID)).thenReturn(true);

        assertError(() -> collectionService.create(new CollectionNameRequestDTO("batel")), ErrorCode.DUPLICATE_NAME);
        assertError(() -> collectionService.rename(BATEL_ID, new CollectionNameRequestDTO("Favoritos")),
                ErrorCode.DUPLICATE_NAME);
        assertThat(batel.getName()).isEqualTo("Batel");
    }

    @Test
    void renamesAndMarksUpdated() {
        CollectionResponseDTO response = collectionService.rename(BATEL_ID, new CollectionNameRequestDTO("Batel 2"));

        assertThat(response.name()).isEqualTo("Batel 2");
        assertThat(response.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void favoritesCannotBeRenamedOrDeleted() {
        assertError(() -> collectionService.rename(FAVORITES_ID, new CollectionNameRequestDTO("Outra")),
                ErrorCode.FAVORITES_LOCKED);
        assertError(() -> collectionService.delete(FAVORITES_ID), ErrorCode.FAVORITES_LOCKED);
        verify(collectionRepository, never()).delete(any(PlantCollection.class));
    }

    @Test
    void deletesRegularCollection() {
        collectionService.delete(BATEL_ID);

        verify(collectionRepository).delete(batel);
    }

    @Test
    void unknownCollectionIsNotFound() {
        when(collectionRepository.findDetailedById(99L)).thenReturn(Optional.empty());

        assertError(() -> collectionService.findById(99L), ErrorCode.RESOURCE_NOT_FOUND);
    }

    private static void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
    }
}

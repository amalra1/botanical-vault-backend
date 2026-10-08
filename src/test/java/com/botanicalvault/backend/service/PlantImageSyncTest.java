package com.botanicalvault.backend.service;

import static com.botanicalvault.backend.TestPlants.ELEVATION;
import static com.botanicalvault.backend.TestPlants.PALM;
import static com.botanicalvault.backend.TestPlants.SMALL;
import static com.botanicalvault.backend.TestPlants.TOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.botanicalvault.backend.TestPlants;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.PlantImageUpdateRequestDTO;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantImage;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.storage.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlantImageSyncTest {
    private static final long ELEVATION_IMAGE_ID = 700L;
    private static final long TOP_IMAGE_ID = 701L;
    private static final long FOREIGN_IMAGE_ID = 999L;

    private final PlantReferenceResolver references = mock(PlantReferenceResolver.class);
    private final ImageStorage imageStorage = mock(ImageStorage.class);
    private final PlantImageSync imageSync = new PlantImageSync(
            references, new PlantImageUploads(), imageStorage, new StoredImageCleanup(imageStorage));
    private final Plant butia = TestPlants.plant(7L, "Butiá", PALM, SMALL, List.of(), List.of(ELEVATION, TOP));

    @BeforeEach
    void setUp() {
        when(references.views(any())).thenReturn(Map.of(1L, ELEVATION, 2L, TOP));
    }

    @Test
    void reordersAndRemovesMissingImages() {
        List<String> removed = imageSync.apply(butia,
                List.of(new PlantImageUpdateRequestDTO(TOP_IMAGE_ID, 2L)), null);

        assertThat(removed).containsExactly("Butiá0.png");
        assertThat(butia.getImages()).singleElement()
                .extracting(PlantImage::getId, PlantImage::getPosition)
                .containsExactly(TOP_IMAGE_ID, 1);
    }

    @Test
    void rejectsImageFromAnotherPlant() {
        assertError(List.of(new PlantImageUpdateRequestDTO(FOREIGN_IMAGE_ID, 1L)), ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void rejectsDuplicateImage() {
        assertError(List.of(new PlantImageUpdateRequestDTO(ELEVATION_IMAGE_ID, 1L),
                new PlantImageUpdateRequestDTO(ELEVATION_IMAGE_ID, 2L)), ErrorCode.DUPLICATE_IMAGE);
    }

    @Test
    void rejectsNewImageWithoutFile() {
        assertError(List.of(new PlantImageUpdateRequestDTO(ELEVATION_IMAGE_ID, 1L),
                PlantImageUpdateRequestDTO.newImage(2L)), ErrorCode.IMAGE_FILES_MISMATCH);
    }

    private void assertError(List<PlantImageUpdateRequestDTO> images, ErrorCode expected) {
        assertThatThrownBy(() -> imageSync.apply(butia, images, List.of()))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
        assertThat(butia.getImages()).hasSize(2);
        verify(imageStorage, never()).save(anyString(), any());
    }
}

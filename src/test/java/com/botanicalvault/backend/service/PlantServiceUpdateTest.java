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
import java.util.Optional;
import java.util.Set;

import com.botanicalvault.backend.TestPlants;
import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.dto.request.PlantImageUpdateRequestDTO;
import com.botanicalvault.backend.dto.request.PlantUpdateRequestDTO;
import com.botanicalvault.backend.dto.response.PlantImageResponseDTO;
import com.botanicalvault.backend.dto.response.PlantResponseDTO;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.repository.PlantRepository;
import com.botanicalvault.backend.storage.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class PlantServiceUpdateTest {
    private static final long PLANT_ID = 7L;
    private static final long ELEVATION_IMAGE_ID = 700L;
    private static final long TOP_IMAGE_ID = 701L;

    private final PlantRepository plantRepository = mock(PlantRepository.class);
    private final PlantReferenceResolver references = mock(PlantReferenceResolver.class);
    private final ImageStorage imageStorage = mock(ImageStorage.class);
    private final StoredImageCleanup cleanup = new StoredImageCleanup(imageStorage);
    private final PlantService plantService = new PlantService(plantRepository, new PlantFields(references),
            new PlantImageSync(references, new PlantImageUploads(), imageStorage, cleanup), imageStorage, cleanup);
    private final Plant butia = TestPlants.plant(PLANT_ID, "Butiá", PALM, SMALL, List.of(), List.of(ELEVATION, TOP));

    @BeforeEach
    void setUp() {
        when(references.type(4L)).thenReturn(PALM);
        when(references.size(1L)).thenReturn(SMALL);
        when(references.origins(List.of())).thenReturn(Set.of());
        when(references.views(any())).thenReturn(Map.of(1L, ELEVATION, 2L, TOP));
        when(plantRepository.findDetailedById(PLANT_ID)).thenReturn(Optional.of(butia));
        when(plantRepository.saveAndFlush(any(Plant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(imageStorage.readUrl(anyString())).thenAnswer(invocation -> "url:" + invocation.getArgument(0));
    }

    @Test
    void updatesFieldsReordersImagesAndDeletesRemovedFiles() {
        List<PlantImageUpdateRequestDTO> images = List.of(
                new PlantImageUpdateRequestDTO(TOP_IMAGE_ID, 1L), PlantImageUpdateRequestDTO.newImage(2L));
        MockMultipartFile added = new MockMultipartFile("files", "butia-top.png", "image/png",
                TestPngs.transparent(2, 2));

        PlantResponseDTO response = plantService.update(PLANT_ID, request("  Butiá-da-serra ", images), List.of(added));

        assertThat(response.commonName()).isEqualTo("Butiá-da-serra");
        assertThat(response.notes()).isNull();
        assertThat(response.images()).extracting(image -> image.view().name())
                .containsExactly("Elevação", "Vista superior");
        assertThat(response.images()).extracting(PlantImageResponseDTO::originalFilename)
                .containsExactly("Butiá.png", "butia-top.png");
        verify(imageStorage).save(anyString(), any());
        verify(imageStorage).delete("Butiá0.png");
        verify(imageStorage, never()).delete("Butiá1.png");
    }

    @Test
    void keepsExistingImagesWithoutFiles() {
        List<PlantImageUpdateRequestDTO> images = List.of(
                new PlantImageUpdateRequestDTO(ELEVATION_IMAGE_ID, 1L), new PlantImageUpdateRequestDTO(TOP_IMAGE_ID, 2L));

        PlantResponseDTO response = plantService.update(PLANT_ID, request("Butiá", images), null);

        assertThat(response.images()).hasSize(2);
        verify(imageStorage, never()).save(anyString(), any());
        verify(imageStorage, never()).delete(anyString());
    }

    @Test
    void deletesPlantAndItsFiles() {
        plantService.delete(PLANT_ID);

        verify(plantRepository).delete(butia);
        verify(imageStorage).delete("Butiá0.png");
        verify(imageStorage).delete("Butiá1.png");
    }

    @Test
    void updateAndDeleteRejectUnknownPlant() {
        when(plantRepository.findDetailedById(99L)).thenReturn(Optional.empty());
        List<PlantImageUpdateRequestDTO> images = List.of(PlantImageUpdateRequestDTO.newImage(1L));

        assertThatThrownBy(() -> plantService.update(99L, request("Butiá", images), List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> plantService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(plantRepository, never()).delete(any(Plant.class));
    }

    private static PlantUpdateRequestDTO request(String commonName, List<PlantImageUpdateRequestDTO> images) {
        return new PlantUpdateRequestDTO(commonName, null, 4L, 1L, null, " ", images);
    }
}

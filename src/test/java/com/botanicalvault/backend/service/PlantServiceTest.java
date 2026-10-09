package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.constants.LightExposure;
import com.botanicalvault.backend.dto.request.PlantCreateRequestDTO;
import com.botanicalvault.backend.dto.request.PlantImageRequestDTO;
import com.botanicalvault.backend.dto.response.PlantResponseDTO;
import com.botanicalvault.backend.entity.ImageView;
import com.botanicalvault.backend.entity.Plant;
import com.botanicalvault.backend.entity.PlantType;
import com.botanicalvault.backend.entity.Size;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.repository.PlantCollectionRepository;
import com.botanicalvault.backend.repository.PlantRepository;
import com.botanicalvault.backend.storage.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class PlantServiceTest {
    private static final ImageView ELEVATION = ImageView.builder().id(1L).name("Elevação").position(1).build();
    private static final ImageView TOP = ImageView.builder().id(2L).name("Vista superior").position(2).build();

    private final PlantRepository plantRepository = mock(PlantRepository.class);
    private final PlantReferenceResolver references = mock(PlantReferenceResolver.class);
    private final ImageStorage imageStorage = mock(ImageStorage.class);
    private final StoredImageCleanup cleanup = new StoredImageCleanup(imageStorage);
    private final PlantCollectionRepository collectionRepository = mock(PlantCollectionRepository.class);
    private final PlantService plantService = new PlantService(plantRepository, new PlantFields(references),
            new PlantImageSync(references, new PlantImageUploads(), imageStorage, cleanup), imageStorage, cleanup,
            collectionRepository);

    @BeforeEach
    void setUp() {
        when(references.type(4L)).thenReturn(PlantType.builder().id(4L).name("Palmeira").position(4).build());
        when(references.size(2L)).thenReturn(Size.builder().id(2L).name("Médio").description("M · média").position(2).build());
        when(references.origins(List.of())).thenReturn(Set.of());
        when(references.views(any())).thenReturn(Map.of(1L, ELEVATION, 2L, TOP));
        when(plantRepository.save(any(Plant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(imageStorage.readUrl(anyString())).thenAnswer(invocation -> "url:" + invocation.getArgument(0));
    }

    @Test
    void storesImagesInOrderWithPositions() {
        PlantResponseDTO response = plantService.create(
                request("  Butiá ", " ", List.of(1L, 2L)),
                List.of(png("butia-elevacao.png"), png("butia-vista-superior.png")));

        assertThat(response.commonName()).isEqualTo("Butiá");
        assertThat(response.scientificName()).isNull();
        assertThat(response.images()).extracting(image -> image.view().name())
                .containsExactly("Elevação", "Vista superior");
        assertThat(response.images()).extracting(image -> image.originalFilename())
                .containsExactly("butia-elevacao.png", "butia-vista-superior.png");
        assertThat(response.images()).allSatisfy(image -> assertThat(image.url()).startsWith("url:").endsWith(".png"));
        verify(imageStorage, times(2)).save(anyString(), any());
    }

    @Test
    void storesLightExposuresInScaleOrder() {
        List<PlantImageRequestDTO> images = List.of(new PlantImageRequestDTO(1L));
        PlantCreateRequestDTO dto = new PlantCreateRequestDTO("Butiá", null, 4L, 2L, null,
                List.of(LightExposure.FULL_SUN, LightExposure.PARTIAL_SHADE), null, images);

        PlantResponseDTO response = plantService.create(dto, List.of(png("butia.png")));

        assertThat(response.lightExposures()).containsExactly(LightExposure.PARTIAL_SHADE, LightExposure.FULL_SUN);
    }

    @Test
    void lightExposuresAreOptional() {
        PlantResponseDTO response = plantService.create(request("Butiá", null, List.of(1L)), List.of(png("a.png")));

        assertThat(response.lightExposures()).isEmpty();
    }

    @Test
    void rejectsMismatchBetweenImagesAndFiles() {
        assertError(() -> plantService.create(request("Butiá", null, List.of(1L, 2L)), List.of(png("a.png"))),
                ErrorCode.IMAGE_FILES_MISMATCH);
        verify(imageStorage, never()).save(anyString(), any());
    }

    @Test
    void rejectsFilesThatAreNotPng() {
        MultipartFile jpeg = new MockMultipartFile("files", "photo.png", "image/png", new byte[] {(byte) 0xFF, (byte) 0xD8});

        assertError(() -> plantService.create(request("Butiá", null, List.of(1L)), List.of(jpeg)),
                ErrorCode.INVALID_IMAGE_FORMAT);
        verify(imageStorage, never()).save(anyString(), any());
    }

    @Test
    void unknownTypeStoresNothing() {
        when(references.type(4L)).thenThrow(new ResourceNotFoundException("Plant type", 4L));

        assertError(() -> plantService.create(request("Butiá", null, List.of(1L)), List.of(png("a.png"))),
                ErrorCode.RESOURCE_NOT_FOUND);
        verify(imageStorage, never()).save(anyString(), any());
    }

    private static PlantCreateRequestDTO request(String commonName, String scientificName, List<Long> viewIds) {
        List<PlantImageRequestDTO> images = viewIds.stream().map(PlantImageRequestDTO::new).toList();
        return new PlantCreateRequestDTO(commonName, scientificName, 4L, 2L, null, null, null, images);
    }

    private static MultipartFile png(String filename) {
        return new MockMultipartFile("files", filename, "image/png", TestPngs.transparent(2, 2));
    }

    private static void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
    }
}

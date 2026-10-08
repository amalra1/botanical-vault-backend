package com.botanicalvault.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.constants.PlantSort;
import com.botanicalvault.backend.dto.request.PlantSearchRequestDTO;
import com.botanicalvault.backend.dto.response.FacetResponseDTO;
import com.botanicalvault.backend.dto.response.OriginFacetResponseDTO;
import com.botanicalvault.backend.dto.response.PlantFacetsResponseDTO;
import com.botanicalvault.backend.dto.response.PlantResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSearchResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSummaryResponseDTO;
import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.service.AuthUserDetailsService;
import com.botanicalvault.backend.service.JwtService;
import com.botanicalvault.backend.service.PlantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@WebMvcTest(PlantController.class)
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class PlantControllerTest {
    private static final String EMAIL = "architect@test.local";
    private static final String BEARER = "Bearer valid-token";
    private static final String VALID_PLANT =
            "{\"commonName\":\"Butiá\",\"typeId\":4,\"sizeId\":2,\"images\":[{\"viewId\":1}]}";
    private static final PlantResponseDTO CREATED = new PlantResponseDTO(
            7L, "Butiá", null, new TaxonomyItemResponseDTO(4L, "Palmeira"),
            new SizeResponseDTO(2L, "Médio", "M · média"), List.of(), null, List.of(),
            Instant.parse("2026-10-07T12:00:00Z"), Instant.parse("2026-10-07T12:00:00Z"));
    private static final FacetResponseDTO EMPTY_FACET = new FacetResponseDTO(1, List.of());
    private static final PlantSearchResponseDTO SEARCH_RESULT = new PlantSearchResponseDTO(
            List.of(new PlantSummaryResponseDTO(7L, "Butiá", null, new TaxonomyItemResponseDTO(4L, "Palmeira"),
                    new SizeResponseDTO(2L, "Médio", "M · média"), List.of(), 2, "url")),
            new PlantFacetsResponseDTO(EMPTY_FACET, new OriginFacetResponseDTO(1, 1, List.of()), EMPTY_FACET, EMPTY_FACET));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlantService plantService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(jwtService.readSubject("valid-token")).thenReturn(Optional.of(EMAIL));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(User.builder().email(EMAIL).build()));
    }

    @Test
    void createRequiresToken() throws Exception {
        mockMvc.perform(createRequest(VALID_PLANT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRejectsInvalidToken() throws Exception {
        mockMvc.perform(createRequest(VALID_PLANT).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsPlant() throws Exception {
        when(plantService.create(any(), anyList())).thenReturn(CREATED);

        mockMvc.perform(createRequest(VALID_PLANT).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/plants/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.size.description").value("M · média"));
    }

    @Test
    void createValidatesPlantPart() throws Exception {
        String withoutName = "{\"typeId\":4,\"sizeId\":2,\"images\":[{\"viewId\":1}]}";

        mockMvc.perform(createRequest(withoutName).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.fields.commonName").exists());
    }

    @Test
    void createReportsInvalidImageFormat() throws Exception {
        when(plantService.create(any(), anyList()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_IMAGE_FORMAT, "Only PNG images are accepted"));

        mockMvc.perform(createRequest(VALID_PLANT).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value(ErrorCode.INVALID_IMAGE_FORMAT.name()));
    }

    @Test
    void findByIdReturnsNotFound() throws Exception {
        when(plantService.findById(99L)).thenThrow(new ResourceNotFoundException("Plant", 99L));

        mockMvc.perform(get("/plants/99").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void searchRequiresToken() throws Exception {
        mockMvc.perform(get("/plants")).andExpect(status().isUnauthorized());
    }

    @Test
    void searchRejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/plants").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchBindsFilters() throws Exception {
        PlantSearchRequestDTO expected = new PlantSearchRequestDTO("buti", 4L, null, true, 2L, 1L, PlantSort.RECENT);
        when(plantService.search(expected)).thenReturn(SEARCH_RESULT);

        mockMvc.perform(get("/plants")
                        .param("q", "buti").param("typeId", "4").param("unclassified", "true")
                        .param("sizeId", "2").param("viewId", "1").param("sort", "RECENT")
                        .header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants[0].coverUrl").value("url"))
                .andExpect(jsonPath("$.plants[0].imageCount").value(2))
                .andExpect(jsonPath("$.facets.origins.unclassified").value(1));
        verify(plantService).search(expected);
    }

    @Test
    void searchDefaultsToNameSort() throws Exception {
        PlantSearchRequestDTO expected = new PlantSearchRequestDTO(null, null, null, false, null, null, PlantSort.NAME);
        when(plantService.search(expected)).thenReturn(SEARCH_RESULT);

        mockMvc.perform(get("/plants").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isOk());
        verify(plantService).search(expected);
    }

    @Test
    void searchRejectsTooLongQuery() throws Exception {
        mockMvc.perform(get("/plants").param("q", "a".repeat(161)).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.fields.q").exists());
    }

    @Test
    void searchRejectsUnknownSort() throws Exception {
        mockMvc.perform(get("/plants").param("sort", "POPULAR").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.VALIDATION_FAILED.name()));
    }

    @Test
    void searchReportsConflictingFilters() throws Exception {
        when(plantService.search(any()))
                .thenThrow(new BusinessException(ErrorCode.CONFLICTING_FILTERS, "Use either originId or unclassified"));

        mockMvc.perform(get("/plants").param("originId", "1").param("unclassified", "true")
                        .header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.CONFLICTING_FILTERS.name()));
    }

    private static MockMultipartHttpServletRequestBuilder createRequest(String plantJson) {
        return multipart("/plants")
                .file(new MockMultipartFile("plant", "", MediaType.APPLICATION_JSON_VALUE, plantJson.getBytes()))
                .file(new MockMultipartFile("files", "butia.png", MediaType.IMAGE_PNG_VALUE, TestPngs.transparent(2, 2)));
    }
}

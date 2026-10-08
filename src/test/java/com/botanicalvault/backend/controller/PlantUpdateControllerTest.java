package com.botanicalvault.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.response.PlantResponseDTO;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@WebMvcTest(PlantController.class)
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class PlantUpdateControllerTest {
    private static final String EMAIL = "architect@test.local";
    private static final String BEARER = "Bearer valid-token";
    private static final String VALID_PLANT =
            "{\"commonName\":\"Butiá\",\"typeId\":4,\"sizeId\":2,\"images\":[{\"id\":3,\"viewId\":1},{\"viewId\":2}]}";
    private static final String KEPT_ONLY =
            "{\"commonName\":\"Butiá\",\"typeId\":4,\"sizeId\":2,\"images\":[{\"id\":3,\"viewId\":1}]}";
    private static final PlantResponseDTO UPDATED = new PlantResponseDTO(
            7L, "Butiá", null, new TaxonomyItemResponseDTO(4L, "Palmeira"),
            new SizeResponseDTO(2L, "Médio", "M · média"), List.of(), null, List.of(),
            Instant.parse("2026-10-07T12:00:00Z"), Instant.parse("2026-10-07T13:00:00Z"));

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
    void updateRequiresToken() throws Exception {
        mockMvc.perform(updateRequest(VALID_PLANT)).andExpect(status().isUnauthorized());
    }

    @Test
    void updateRejectsInvalidToken() throws Exception {
        mockMvc.perform(updateRequest(VALID_PLANT).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatesPlant() throws Exception {
        when(plantService.update(eq(7L), any(), anyList())).thenReturn(UPDATED);

        mockMvc.perform(updateRequest(VALID_PLANT)
                        .file(new MockMultipartFile("files", "new.png", MediaType.IMAGE_PNG_VALUE, TestPngs.transparent(2, 2)))
                        .header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void updateAcceptsRequestWithoutFiles() throws Exception {
        when(plantService.update(eq(7L), any(), isNull())).thenReturn(UPDATED);

        mockMvc.perform(updateRequest(KEPT_ONLY).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isOk());
        verify(plantService).update(eq(7L), any(), isNull());
    }

    @Test
    void updateValidatesImages() throws Exception {
        String withoutImages = "{\"commonName\":\"Butiá\",\"typeId\":4,\"sizeId\":2,\"images\":[]}";

        mockMvc.perform(updateRequest(withoutImages).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.fields.images").exists());
    }

    @Test
    void updateReportsDuplicateImage() throws Exception {
        when(plantService.update(eq(7L), any(), any()))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_IMAGE, "Image listed more than once: 3"));

        mockMvc.perform(updateRequest(KEPT_ONLY).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.DUPLICATE_IMAGE.name()));
    }

    @Test
    void deleteRequiresToken() throws Exception {
        mockMvc.perform(delete("/plants/7")).andExpect(status().isUnauthorized());
    }

    @Test
    void deleteRejectsInvalidToken() throws Exception {
        mockMvc.perform(delete("/plants/7").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletesPlant() throws Exception {
        mockMvc.perform(delete("/plants/7").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNoContent());
        verify(plantService).delete(7L);
    }

    @Test
    void deleteReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Plant", 99L)).when(plantService).delete(99L);

        mockMvc.perform(delete("/plants/99").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private static MockMultipartHttpServletRequestBuilder updateRequest(String plantJson) {
        return multipart(HttpMethod.PUT, "/plants/7")
                .file(new MockMultipartFile("plant", "", MediaType.APPLICATION_JSON_VALUE, plantJson.getBytes()));
    }
}

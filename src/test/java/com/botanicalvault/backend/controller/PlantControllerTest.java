package com.botanicalvault.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

    private static MockMultipartHttpServletRequestBuilder createRequest(String plantJson) {
        return multipart("/plants")
                .file(new MockMultipartFile("plant", "", MediaType.APPLICATION_JSON_VALUE, plantJson.getBytes()))
                .file(new MockMultipartFile("files", "butia.png", MediaType.IMAGE_PNG_VALUE, TestPngs.transparent(2, 2)));
    }
}

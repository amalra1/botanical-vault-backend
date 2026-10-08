package com.botanicalvault.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.response.CollectionResponseDTO;
import com.botanicalvault.backend.dto.response.CollectionSummaryResponseDTO;
import com.botanicalvault.backend.dto.response.SizeCoverageResponseDTO;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.service.AuthUserDetailsService;
import com.botanicalvault.backend.service.CollectionService;
import com.botanicalvault.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CollectionController.class)
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class CollectionControllerTest {
    private static final String EMAIL = "architect@test.local";
    private static final String BEARER = "Bearer valid-token";
    private static final Instant UPDATED_AT = Instant.parse("2026-10-08T12:00:00Z");
    private static final List<SizeCoverageResponseDTO> COVERAGE =
            List.of(new SizeCoverageResponseDTO(1L, "Pequeno", "P · baixa", true));
    private static final CollectionResponseDTO BATEL =
            new CollectionResponseDTO(2L, "Batel", false, List.of(), COVERAGE, UPDATED_AT);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollectionService collectionService;

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
    void listRequiresValidToken() throws Exception {
        mockMvc.perform(get("/collections")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/collections").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsCollections() throws Exception {
        when(collectionService.findAll()).thenReturn(List.of(new CollectionSummaryResponseDTO(
                1L, "Favoritos", true, 1, List.of("url"), COVERAGE, UPDATED_AT)));

        mockMvc.perform(get("/collections").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].favorites").value(true))
                .andExpect(jsonPath("$[0].mosaicUrls[0]").value("url"))
                .andExpect(jsonPath("$[0].sizeCoverage[0].covered").value(true));
    }

    @Test
    void createsCollection() throws Exception {
        when(collectionService.create(any())).thenReturn(BATEL);

        mockMvc.perform(post("/collections").header(HttpHeaders.AUTHORIZATION, BEARER)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Batel\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/collections/2"))
                .andExpect(jsonPath("$.name").value("Batel"));
    }

    @Test
    void createRequiresValidTokenAndName() throws Exception {
        mockMvc.perform(post("/collections").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Batel\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/collections").header(HttpHeaders.AUTHORIZATION, BEARER)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void findsCollection() throws Exception {
        when(collectionService.findById(2L)).thenReturn(BATEL);

        mockMvc.perform(get("/collections/2").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
        mockMvc.perform(get("/collections/2").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void renameReportsLockedFavorites() throws Exception {
        when(collectionService.rename(eq(1L), any()))
                .thenThrow(new BusinessException(ErrorCode.FAVORITES_LOCKED, "Favorites cannot be renamed"));

        mockMvc.perform(patch("/collections/1").header(HttpHeaders.AUTHORIZATION, BEARER)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Outra\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ErrorCode.FAVORITES_LOCKED.name()));
        mockMvc.perform(patch("/collections/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Outra\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletesCollection() throws Exception {
        mockMvc.perform(delete("/collections/2").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNoContent());
        verify(collectionService).delete(2L);
    }

    @Test
    void deleteReportsLockedFavoritesAndRequiresToken() throws Exception {
        doThrow(new BusinessException(ErrorCode.FAVORITES_LOCKED, "Favorites cannot be deleted"))
                .when(collectionService).delete(1L);

        mockMvc.perform(delete("/collections/1").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/collections/1").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }
}

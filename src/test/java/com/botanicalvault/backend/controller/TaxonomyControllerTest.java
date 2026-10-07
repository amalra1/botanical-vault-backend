package com.botanicalvault.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.service.AuthUserDetailsService;
import com.botanicalvault.backend.service.ImageViewService;
import com.botanicalvault.backend.service.JwtService;
import com.botanicalvault.backend.service.OriginService;
import com.botanicalvault.backend.service.PlantTypeService;
import com.botanicalvault.backend.service.SizeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({OriginController.class, PlantTypeController.class, SizeController.class, ImageViewController.class})
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class TaxonomyControllerTest {
    private static final String EMAIL = "architect@test.local";
    private static final String TOKEN = "valid-token";
    private static final String ITEM_NAME = "Item";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OriginService originService;

    @MockitoBean
    private PlantTypeService plantTypeService;

    @MockitoBean
    private SizeService sizeService;

    @MockitoBean
    private ImageViewService imageViewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        List<TaxonomyItemResponseDTO> items = List.of(new TaxonomyItemResponseDTO(1L, ITEM_NAME));
        when(originService.findAll()).thenReturn(items);
        when(plantTypeService.findAll()).thenReturn(items);
        when(imageViewService.findAll()).thenReturn(items);
        when(sizeService.findAll()).thenReturn(List.of(new SizeResponseDTO(1L, ITEM_NAME, "P · baixa")));
        when(jwtService.readSubject(TOKEN)).thenReturn(Optional.of(EMAIL));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(User.builder().email(EMAIL).build()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/origins", "/plant-types", "/sizes", "/image-views"})
    void requiresToken(String route) throws Exception {
        mockMvc.perform(get(route))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/origins", "/plant-types", "/sizes", "/image-views"})
    void rejectsInvalidToken(String route) throws Exception {
        mockMvc.perform(get(route).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/origins", "/plant-types", "/sizes", "/image-views"})
    void returnsItemsWithValidToken(String route) throws Exception {
        mockMvc.perform(get(route).header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(ITEM_NAME));
    }

    @Test
    void sizesIncludeDescription() throws Exception {
        mockMvc.perform(get("/sizes").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("P · baixa"));
    }
}

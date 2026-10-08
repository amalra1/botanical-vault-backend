package com.botanicalvault.backend.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.service.AuthUserDetailsService;
import com.botanicalvault.backend.service.CollectionMembershipService;
import com.botanicalvault.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({CollectionPlantController.class, PlantFavoriteController.class})
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class CollectionPlantControllerTest {
    private static final String EMAIL = "architect@test.local";
    private static final String BEARER = "Bearer valid-token";
    private static final String MEMBER_PATH = "/collections/2/plants/7";
    private static final String FAVORITE_PATH = "/plants/7/favorite";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollectionMembershipService membershipService;

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
    void addsAndRemovesPlant() throws Exception {
        mockMvc.perform(put(MEMBER_PATH).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete(MEMBER_PATH).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNoContent());

        verify(membershipService).addPlant(2L, 7L);
        verify(membershipService).removePlant(2L, 7L);
    }

    @Test
    void reportsUnknownPlant() throws Exception {
        doThrow(new ResourceNotFoundException("Plant", 7L)).when(membershipService).addPlant(2L, 7L);

        mockMvc.perform(put(MEMBER_PATH).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void favoritesAndUnfavoritesPlant() throws Exception {
        mockMvc.perform(put(FAVORITE_PATH).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete(FAVORITE_PATH).header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNoContent());

        verify(membershipService).favorite(7L);
        verify(membershipService).unfavorite(7L);
    }

    @Test
    void everyRouteRequiresValidToken() throws Exception {
        for (String path : new String[] {MEMBER_PATH, FAVORITE_PATH}) {
            mockMvc.perform(put(path)).andExpect(status().isUnauthorized());
            mockMvc.perform(delete(path)).andExpect(status().isUnauthorized());
            mockMvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                    .andExpect(status().isUnauthorized());
            mockMvc.perform(delete(path).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                    .andExpect(status().isUnauthorized());
        }
    }
}

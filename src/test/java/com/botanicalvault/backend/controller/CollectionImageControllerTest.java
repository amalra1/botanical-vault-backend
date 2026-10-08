package com.botanicalvault.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.dto.response.CollectionImageResponseDTO;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CollectionController.class)
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class CollectionImageControllerTest {
    private static final String EMAIL = "architect@test.local";
    private static final String BEARER = "Bearer valid-token";
    private static final Long BATEL_ID = 2L;
    private static final Long UNKNOWN_ID = 99L;

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
    void listsCollectionImages() throws Exception {
        when(collectionService.findImages(BATEL_ID)).thenReturn(List.of(
                new CollectionImageResponseDTO(5L, "Jerivá", 50L, "jeriva.png", "url")));

        mockMvc.perform(get("/collections/2/images").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plantId").value(5))
                .andExpect(jsonPath("$[0].commonName").value("Jerivá"))
                .andExpect(jsonPath("$[0].imageId").value(50))
                .andExpect(jsonPath("$[0].originalFilename").value("jeriva.png"))
                .andExpect(jsonPath("$[0].url").value("url"));
    }

    @Test
    void requiresValidToken() throws Exception {
        mockMvc.perform(get("/collections/2/images")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/collections/2/images").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownCollectionIsNotFound() throws Exception {
        when(collectionService.findImages(UNKNOWN_ID)).thenThrow(new ResourceNotFoundException("Collection", UNKNOWN_ID));

        mockMvc.perform(get("/collections/99/images").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isNotFound());
    }
}

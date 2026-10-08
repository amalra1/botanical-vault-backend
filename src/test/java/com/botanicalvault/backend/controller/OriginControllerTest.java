package com.botanicalvault.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.service.AuthUserDetailsService;
import com.botanicalvault.backend.service.JwtService;
import com.botanicalvault.backend.service.OriginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(OriginController.class)
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class OriginControllerTest {
    private static final String EMAIL = "architect@test.local";
    private static final String BEARER = "Bearer valid-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OriginService originService;

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
        mockMvc.perform(createRequest("{\"name\":\"Exótica\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsOrigin() throws Exception {
        when(originService.create(any())).thenReturn(new TaxonomyItemResponseDTO(3L, "Exótica"));

        mockMvc.perform(createRequest("{\"name\":\"Exótica\"}").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Exótica"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mockMvc.perform(createRequest("{\"name\":\"  \"}").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void createReportsDuplicate() throws Exception {
        when(originService.create(any()))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_NAME, "Origin already exists"));

        mockMvc.perform(createRequest("{\"name\":\"Mata Atlântica\"}").header(HttpHeaders.AUTHORIZATION, BEARER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ErrorCode.DUPLICATE_NAME.name()));
    }

    private static MockHttpServletRequestBuilder createRequest(String body) {
        return post("/origins").contentType(MediaType.APPLICATION_JSON).content(body);
    }
}

package com.botanicalvault.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.dto.response.HealthResponseDTO;
import com.botanicalvault.backend.service.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @Test
    void healthIsPublicAndReportsUp() throws Exception {
        when(healthService.check()).thenReturn(new HealthResponseDTO("UP"));

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void otherRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/plants"))
                .andExpect(status().isUnauthorized());
    }
}

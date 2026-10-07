package com.botanicalvault.backend.integration;

import static com.botanicalvault.backend.integration.AuthTestSupport.bearerToken;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.botanicalvault.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaxonomyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"/origins", "/plant-types", "/sizes", "/image-views"})
    void requiresToken(String route) throws Exception {
        mockMvc.perform(get(route))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsSeededTaxonomiesInOrder() throws Exception {
        String token = bearerToken(mockMvc);

        mockMvc.perform(get("/origins").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Floresta Ombrófila Mista", "Mata Atlântica")));
        mockMvc.perform(get("/plant-types").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Forração", "Arbusto", "Árvore", "Palmeira")));
        mockMvc.perform(get("/sizes").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Pequeno", "Médio", "Grande")))
                .andExpect(jsonPath("$[*].description").value(contains("P · baixa", "M · média", "G · alta")));
        mockMvc.perform(get("/image-views").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Elevação", "Vista superior", "Perspectiva")));
    }
}

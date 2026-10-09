package com.botanicalvault.backend.integration;

import static com.botanicalvault.backend.integration.AuthTestSupport.EMAIL;
import static com.botanicalvault.backend.integration.AuthTestSupport.PASSWORD;
import static com.botanicalvault.backend.integration.AuthTestSupport.login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.botanicalvault.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.login-throttle.max-failures-per-client=2",
    "app.login-throttle.max-failures-total=100"
})
class LoginThrottleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void blocksLoginAfterRepeatedFailuresEvenWithTheRightPassword() throws Exception {
        mockMvc.perform(login(EMAIL, "wrong-password")).andExpect(status().isUnauthorized());
        mockMvc.perform(login(EMAIL, "wrong-password")).andExpect(status().isUnauthorized());

        mockMvc.perform(login(EMAIL, PASSWORD))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.error").value("TOO_MANY_LOGIN_ATTEMPTS"))
                .andExpect(jsonPath("$.fields.retryAfterSeconds").exists());
    }
}

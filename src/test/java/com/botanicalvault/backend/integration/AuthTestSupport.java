package com.botanicalvault.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

final class AuthTestSupport {
    static final String EMAIL = "architect@test.local";
    static final String PASSWORD = "test-password";
    private static final Pattern TOKEN_FIELD = Pattern.compile("\"token\":\"([^\"]+)\"");

    private AuthTestSupport() {
    }

    static RequestBuilder login(String email, String password) {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        return post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    static String bearerToken(MockMvc mockMvc) throws Exception {
        String body = mockMvc.perform(login(EMAIL, PASSWORD))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = TOKEN_FIELD.matcher(body);
        assertThat(matcher.find()).isTrue();
        return "Bearer " + matcher.group(1);
    }
}

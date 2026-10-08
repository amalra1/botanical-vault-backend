package com.botanicalvault.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.service.AuthUserDetailsService;
import com.botanicalvault.backend.service.JwtService;
import com.botanicalvault.backend.storage.LocalFileUrlSigner;
import com.botanicalvault.backend.storage.LocalImageStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LocalFileController.class)
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class LocalFileControllerTest {
    private static final String KEY = "abc.png";
    private static final String FILE_URL = "/files/" + KEY + "?expires=1791460800&signature=sig";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocalFileUrlSigner signer;

    @MockitoBean
    private LocalImageStorage storage;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void servesFileWithValidSignatureWithoutToken() throws Exception {
        byte[] png = TestPngs.transparent(2, 2);
        when(signer.isValid(any())).thenReturn(true);
        when(signer.remainingValidity(1791460800L)).thenReturn(Duration.ofMinutes(10));
        when(storage.exists(KEY)).thenReturn(true);
        when(storage.read(KEY)).thenReturn(png);

        mockMvc.perform(get(FILE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(png));
    }

    @Test
    void rejectsInvalidSignature() throws Exception {
        when(signer.isValid(any())).thenReturn(false);

        mockMvc.perform(get(FILE_URL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(ErrorCode.INVALID_FILE_SIGNATURE.name()));
    }

    @Test
    void reportsMissingFile() throws Exception {
        when(signer.isValid(any())).thenReturn(true);
        when(storage.exists(KEY)).thenReturn(false);

        mockMvc.perform(get(FILE_URL))
                .andExpect(status().isNotFound());
    }
}

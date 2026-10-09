package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.IntStream;

import com.botanicalvault.backend.config.JwtProperties;
import com.botanicalvault.backend.config.LoginThrottleProperties;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.LoginRequestDTO;
import com.botanicalvault.backend.dto.response.LoginResponseDTO;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.exception.LoginThrottledException;
import com.botanicalvault.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
    private static final String SECRET = "dGVzdC1vbmx5LXNlY3JldC1mb3ItYm90YW5pY2FsLXZhdWx0LXRlc3Rz";
    private static final Instant NOW = Instant.parse("2026-10-07T12:00:00Z");
    private static final String EMAIL = "architect@test.local";
    private static final String PASSWORD = "test-password";
    private static final String CLIENT = "203.0.113.7";
    private static final int MAX_FAILURES = 3;

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = spy(new BCryptPasswordEncoder());
    private final JwtService jwtService =
            new JwtService(new JwtProperties(SECRET, Duration.ofDays(7)), Clock.fixed(NOW, ZoneOffset.UTC));
    private AuthService authService;

    @BeforeEach
    void setUp() {
        LoginThrottle throttle = new LoginThrottle(new LoginThrottleProperties(
                MAX_FAILURES, MAX_FAILURES * MAX_FAILURES, Duration.ofMinutes(15), 10, false),
                Clock.fixed(NOW, ZoneOffset.UTC));
        authService = new AuthService(
                new AuthUserDetailsService(userRepository), passwordEncoder, jwtService, throttle);
        User user = User.builder().email(EMAIL).passwordHash(passwordEncoder.encode(PASSWORD)).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    void validCredentialsReturnTokenForTheUser() {
        LoginResponseDTO response = authService.login(new LoginRequestDTO(EMAIL, PASSWORD), CLIENT);

        assertThat(jwtService.readSubject(response.token())).contains(EMAIL);
        assertThat(response.expiresAt()).isAfter(NOW);
    }

    @Test
    void emailIsNormalizedBeforeLookup() {
        LoginResponseDTO response = authService.login(new LoginRequestDTO("  Architect@Test.LOCAL ", PASSWORD), CLIENT);

        assertThat(jwtService.readSubject(response.token())).contains(EMAIL);
    }

    @Test
    void wrongPasswordIsRejected() {
        assertInvalidCredentials(new LoginRequestDTO(EMAIL, "wrong-password"));
    }

    @Test
    void unknownEmailIsRejected() {
        assertInvalidCredentials(new LoginRequestDTO("someone@test.local", PASSWORD));
    }

    @Test
    void throttledClientIsRejectedBeforeCheckingThePassword() {
        LoginRequestDTO wrong = new LoginRequestDTO(EMAIL, "wrong-password");
        IntStream.range(0, MAX_FAILURES).forEach(attempt -> assertInvalidCredentials(wrong));
        clearInvocations(passwordEncoder);

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), CLIENT))
                .isInstanceOf(LoginThrottledException.class);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void successfulLoginResetsTheClientFailures() {
        LoginRequestDTO wrong = new LoginRequestDTO(EMAIL, "wrong-password");
        IntStream.range(0, MAX_FAILURES - 1).forEach(attempt -> assertInvalidCredentials(wrong));
        authService.login(new LoginRequestDTO(EMAIL, PASSWORD), CLIENT);
        IntStream.range(0, MAX_FAILURES - 1).forEach(attempt -> assertInvalidCredentials(wrong));

        assertThat(authService.login(new LoginRequestDTO(EMAIL, PASSWORD), CLIENT).token()).isNotBlank();
    }

    private void assertInvalidCredentials(LoginRequestDTO dto) {
        assertThatThrownBy(() -> authService.login(dto, CLIENT))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }
}

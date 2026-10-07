package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import com.botanicalvault.backend.config.JwtProperties;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "dGVzdC1vbmx5LXNlY3JldC1mb3ItYm90YW5pY2FsLXZhdWx0LXRlc3Rz";
    private static final String OTHER_SECRET = "YW5vdGhlci1zZWNyZXQtdGhhdC1pcy1sb25nLWVub3VnaC1mb3ItaHMyNTY=";
    private static final Duration EXPIRATION = Duration.ofHours(1);
    private static final Instant NOW = Instant.parse("2026-10-07T12:00:00Z");
    private static final String SUBJECT = "architect@test.local";

    @Test
    void issuedTokenCarriesSubjectAndExpiration() {
        JwtService jwtService = jwtServiceAt(NOW, SECRET);

        IssuedToken issued = jwtService.issue(SUBJECT);

        assertThat(issued.expiresAt()).isEqualTo(NOW.plus(EXPIRATION));
        assertThat(jwtService.readSubject(issued.token())).contains(SUBJECT);
    }

    @Test
    void expiredTokenIsRejected() {
        IssuedToken issued = jwtServiceAt(NOW, SECRET).issue(SUBJECT);
        JwtService later = jwtServiceAt(NOW.plus(EXPIRATION).plusSeconds(1), SECRET);

        assertThat(later.readSubject(issued.token())).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        IssuedToken issued = jwtServiceAt(NOW, OTHER_SECRET).issue(SUBJECT);

        assertThat(jwtServiceAt(NOW, SECRET).readSubject(issued.token())).isEmpty();
    }

    @Test
    void garbageTokenIsRejected() {
        assertThat(jwtServiceAt(NOW, SECRET).readSubject("not-a-jwt")).isEmpty();
    }

    @Test
    void blankSecretFailsFast() {
        assertThatThrownBy(() -> jwtServiceAt(NOW, ""))
                .isInstanceOf(IllegalStateException.class);
    }

    private static JwtService jwtServiceAt(Instant instant, String secret) {
        return new JwtService(new JwtProperties(secret, EXPIRATION), Clock.fixed(instant, ZoneOffset.UTC));
    }
}

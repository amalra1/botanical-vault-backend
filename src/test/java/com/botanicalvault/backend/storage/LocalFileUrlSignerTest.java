package com.botanicalvault.backend.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import com.botanicalvault.backend.config.LocalStorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

class LocalFileUrlSignerTest {
    static final String SECRET = "dGVzdC1vbmx5LXNlY3JldC1mb3ItYm90YW5pY2FsLXZhdWx0LXRlc3Rz";
    static final String BASE_URL = "http://localhost:8080";
    static final Duration VALIDITY = Duration.ofMinutes(60);
    private static final Instant NOW = Instant.parse("2026-10-07T12:00:00Z");
    private static final String KEY = "abc.png";

    private final LocalFileUrlSigner signer = signerAt(NOW);

    @Test
    void buildsUrlUnderFilesPath() {
        UriComponents url = UriComponentsBuilder.fromUriString(signer.signedUrl(KEY)).build();

        assertThat(url.getPath()).isEqualTo("/files/" + KEY);
        assertThat(url.getQueryParams().getFirst("expires"))
                .isEqualTo(String.valueOf(NOW.plus(VALIDITY).getEpochSecond()));
    }

    @Test
    void acceptsOwnSignature() {
        assertThat(signer.isValid(requestFor(signer.signedUrl(KEY), KEY))).isTrue();
    }

    @Test
    void rejectsTamperedSignature() {
        SignedFileRequest request = requestFor(signer.signedUrl(KEY), KEY);

        assertThat(signer.isValid(new SignedFileRequest(KEY, request.expires(), request.signature() + "x"))).isFalse();
    }

    @Test
    void rejectsSignatureForAnotherKey() {
        assertThat(signer.isValid(requestFor(signer.signedUrl(KEY), "other.png"))).isFalse();
    }

    @Test
    void rejectsExpiredUrl() {
        String url = signer.signedUrl(KEY);
        LocalFileUrlSigner later = signerAt(NOW.plus(VALIDITY).plusSeconds(1));

        assertThat(later.isValid(requestFor(url, KEY))).isFalse();
    }

    static LocalFileUrlSigner signerAt(Instant instant) {
        LocalStorageProperties properties = new LocalStorageProperties("unused", BASE_URL, SECRET, VALIDITY);
        return new LocalFileUrlSigner(properties, Clock.fixed(instant, ZoneOffset.UTC));
    }

    private static SignedFileRequest requestFor(String url, String key) {
        UriComponents components = UriComponentsBuilder.fromUriString(url).build();
        long expires = Long.parseLong(components.getQueryParams().getFirst("expires"));
        return new SignedFileRequest(key, expires, components.getQueryParams().getFirst("signature"));
    }
}

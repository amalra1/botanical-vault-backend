package com.botanicalvault.backend.storage;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.botanicalvault.backend.config.LocalStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalFileUrlSigner {
    private static final String ALGORITHM = "HmacSHA256";
    private static final String FILES_PATH = "/files/{key}";
    private static final Base64.Encoder SIGNATURE_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final byte[] secret;
    private final String publicBaseUrl;
    private final Duration validity;
    private final Clock clock;

    public LocalFileUrlSigner(LocalStorageProperties properties, Clock clock) {
        if (properties.signingSecret() == null || properties.signingSecret().isBlank()) {
            throw new IllegalStateException("storage.local.signing-secret must be set");
        }
        this.secret = Base64.getDecoder().decode(properties.signingSecret());
        this.publicBaseUrl = properties.publicBaseUrl();
        this.validity = properties.signedUrl();
        this.clock = clock;
    }

    public String signedUrl(String key) {
        long expires = clock.instant().plus(validity).getEpochSecond();
        return UriComponentsBuilder.fromUriString(publicBaseUrl)
                .path(FILES_PATH)
                .queryParam("expires", expires)
                .queryParam("signature", sign(key, expires))
                .buildAndExpand(key)
                .toUriString();
    }

    public boolean isValid(SignedFileRequest request) {
        boolean notExpired = clock.instant().isBefore(Instant.ofEpochSecond(request.expires()));
        byte[] expected = sign(request.key(), request.expires()).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = request.signature().getBytes(StandardCharsets.US_ASCII);
        return notExpired && MessageDigest.isEqual(expected, actual);
    }

    public Duration remainingValidity(long expires) {
        return Duration.between(clock.instant(), Instant.ofEpochSecond(expires));
    }

    private String sign(String key, long expires) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            byte[] digest = mac.doFinal((key + ":" + expires).getBytes(StandardCharsets.UTF_8));
            return SIGNATURE_ENCODER.encodeToString(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to sign file url", ex);
        }
    }
}

package com.botanicalvault.backend.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import com.botanicalvault.backend.config.GcsStorageProperties;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GcsImageStorageTest {
    private static final String BUCKET = "botanical-vault-test";
    private static final String KEY = "abc.png";
    private static final byte[] CONTENT = {1, 2, 3};
    private static final Duration VALIDITY = Duration.ofMinutes(60);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneOffset.UTC);
    private static final String SIGNED_URL = "https://storage.googleapis.com/" + BUCKET + "/" + KEY + "?X-Goog-Signature=x";

    private final Storage storage = mock(Storage.class);
    private GcsImageStorage imageStorage;

    @BeforeEach
    void setUp() throws Exception {
        when(storage.signUrl(any(BlobInfo.class), anyLong(), any(TimeUnit.class), any(Storage.SignUrlOption.class)))
                .thenReturn(new URI(SIGNED_URL).toURL());
        imageStorage = new GcsImageStorage(storage, new GcsStorageProperties(BUCKET, VALIDITY), CLOCK);
    }

    @Test
    void savesPngInBucket() {
        ArgumentCaptor<BlobInfo> blob = ArgumentCaptor.forClass(BlobInfo.class);

        imageStorage.save(KEY, CONTENT);

        verify(storage).create(blob.capture(), eq(CONTENT));
        assertThat(blob.getValue().getBlobId()).isEqualTo(BlobId.of(BUCKET, KEY));
        assertThat(blob.getValue().getContentType()).isEqualTo("image/png");
    }

    @Test
    void deletesFromBucket() {
        imageStorage.delete(KEY);

        verify(storage).delete(BlobId.of(BUCKET, KEY));
    }

    @Test
    void signsV4ReadUrlWithConfiguredValidity() {
        ArgumentCaptor<BlobInfo> blob = ArgumentCaptor.forClass(BlobInfo.class);
        ArgumentCaptor<Storage.SignUrlOption> option = ArgumentCaptor.forClass(Storage.SignUrlOption.class);
        Storage.SignUrlOption v4 = Storage.SignUrlOption.withV4Signature();

        String url = imageStorage.readUrl(KEY);

        assertThat(url).isEqualTo(SIGNED_URL);
        verify(storage).signUrl(blob.capture(), eq(VALIDITY.toSeconds()), eq(TimeUnit.SECONDS), option.capture());
        assertThat(blob.getValue().getBlobId()).isEqualTo(BlobId.of(BUCKET, KEY));
        assertThat(option.getValue()).usingRecursiveComparison().isEqualTo(v4);
    }

    @Test
    void reusesSignedUrlForSameKey() {
        imageStorage.readUrl(KEY);
        imageStorage.readUrl(KEY);

        verify(storage, times(1)).signUrl(any(BlobInfo.class), anyLong(), any(TimeUnit.class),
                any(Storage.SignUrlOption.class));
    }

    @Test
    void rejectsMissingBucketName() {
        GcsStorageProperties properties = new GcsStorageProperties(" ", VALIDITY);

        assertThatThrownBy(() -> new GcsImageStorage(storage, properties, CLOCK))
                .isInstanceOf(IllegalStateException.class);
    }
}

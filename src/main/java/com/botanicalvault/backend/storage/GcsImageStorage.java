package com.botanicalvault.backend.storage;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.botanicalvault.backend.config.GcsStorageProperties;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "gcs")
public class GcsImageStorage implements ImageStorage {
    private final Storage storage;
    private final String bucketName;
    private final Duration validity;
    private final SignedUrlCache urlCache;

    public GcsImageStorage(Storage storage, GcsStorageProperties properties, Clock clock) {
        if (properties.bucketName() == null || properties.bucketName().isBlank()) {
            throw new IllegalStateException("gcp.storage.bucket-name must be set");
        }
        this.storage = storage;
        this.bucketName = properties.bucketName();
        this.validity = properties.signedUrl();
        this.urlCache = new SignedUrlCache(clock, validity);
    }

    @Override
    public void save(String key, byte[] content) {
        storage.create(blobInfo(key), content);
    }

    @Override
    public void delete(String key) {
        urlCache.evict(key);
        storage.delete(blobId(key));
    }

    @Override
    public String readUrl(String key) {
        return urlCache.get(key, this::sign);
    }

    private String sign(String key) {
        return storage.signUrl(
                blobInfo(key),
                validity.toSeconds(),
                TimeUnit.SECONDS,
                Storage.SignUrlOption.withV4Signature()).toString();
    }

    private BlobInfo blobInfo(String key) {
        return BlobInfo.newBuilder(blobId(key)).setContentType(MediaType.IMAGE_PNG_VALUE).build();
    }

    private BlobId blobId(String key) {
        return BlobId.of(bucketName, key);
    }
}

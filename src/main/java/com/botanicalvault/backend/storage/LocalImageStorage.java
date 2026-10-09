package com.botanicalvault.backend.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.botanicalvault.backend.config.LocalStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalImageStorage implements ImageStorage {
    private final Path root;
    private final LocalFileUrlSigner signer;

    public LocalImageStorage(LocalStorageProperties properties, LocalFileUrlSigner signer) {
        this.root = Path.of(properties.directory()).toAbsolutePath().normalize();
        this.signer = signer;
    }

    @Override
    public void save(String key, byte[] content) {
        Path target = resolve(key);
        try {
            Files.createDirectories(root);
            Files.write(target, content);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to store image " + key, ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to delete image " + key, ex);
        }
    }

    @Override
    public String readUrl(String key) {
        return signer.signedUrl(key);
    }

    public byte[] read(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to read image " + key, ex);
        }
    }

    public boolean exists(String key) {
        return Files.isRegularFile(resolve(key));
    }

    private Path resolve(String key) {
        Path target = root.resolve(key).normalize();
        if (!root.equals(target.getParent())) {
            throw new IllegalArgumentException("Invalid storage key: " + key);
        }
        return target;
    }
}

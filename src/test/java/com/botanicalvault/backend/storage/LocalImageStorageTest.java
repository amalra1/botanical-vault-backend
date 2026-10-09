package com.botanicalvault.backend.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.time.Instant;

import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.config.LocalStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalImageStorageTest {
    private static final String KEY = "abc.png";

    @TempDir
    Path directory;

    private LocalImageStorage storage;

    @BeforeEach
    void setUp() {
        LocalStorageProperties properties = new LocalStorageProperties(
                directory.toString(),
                LocalFileUrlSignerTest.BASE_URL,
                LocalFileUrlSignerTest.SECRET,
                LocalFileUrlSignerTest.VALIDITY);
        storage = new LocalImageStorage(properties, LocalFileUrlSignerTest.signerAt(Instant.now()));
    }

    @Test
    void savesReadsAndDeletes() {
        byte[] content = TestPngs.transparent(3, 3);

        storage.save(KEY, content);
        assertThat(storage.exists(KEY)).isTrue();
        assertThat(storage.read(KEY)).isEqualTo(content);

        storage.delete(KEY);
        assertThat(storage.exists(KEY)).isFalse();
    }

    @Test
    void readUrlIsSigned() {
        assertThat(storage.readUrl(KEY)).contains("/files/" + KEY).contains("signature=");
    }

    @Test
    void rejectsKeysOutsideTheDirectory() {
        assertThatThrownBy(() -> storage.save("../escape.png", new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.save("nested/inside.png", new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

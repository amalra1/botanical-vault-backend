package com.botanicalvault.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import com.botanicalvault.backend.TestPngs;
import org.junit.jupiter.api.Test;

class PngFilesTest {

    @Test
    void acceptsRealPng() {
        assertThat(PngFiles.hasPngSignature(TestPngs.transparent(2, 2))).isTrue();
    }

    @Test
    void rejectsOtherContent() {
        assertThat(PngFiles.hasPngSignature("GIF89a-not-a-png".getBytes(StandardCharsets.US_ASCII))).isFalse();
    }

    @Test
    void rejectsShortOrMissingContent() {
        assertThat(PngFiles.hasPngSignature(new byte[] {(byte) 0x89, 'P'})).isFalse();
        assertThat(PngFiles.hasPngSignature(null)).isFalse();
    }
}

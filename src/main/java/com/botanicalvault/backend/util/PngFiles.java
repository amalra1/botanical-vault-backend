package com.botanicalvault.backend.util;

import java.util.Arrays;

public final class PngFiles {
    private static final byte[] SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private PngFiles() {
    }

    public static boolean hasPngSignature(byte[] content) {
        return content != null
                && content.length >= SIGNATURE.length
                && Arrays.equals(content, 0, SIGNATURE.length, SIGNATURE, 0, SIGNATURE.length);
    }
}

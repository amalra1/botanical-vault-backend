package com.botanicalvault.backend.util;

import java.util.UUID;

public final class StorageKeys {
    private static final String PNG_EXTENSION = ".png";

    private StorageKeys() {
    }

    public static String newPngKey() {
        return UUID.randomUUID() + PNG_EXTENSION;
    }
}

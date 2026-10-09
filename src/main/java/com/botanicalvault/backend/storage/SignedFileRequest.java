package com.botanicalvault.backend.storage;

public record SignedFileRequest(
    String key,
    long expires,
    String signature
) {}

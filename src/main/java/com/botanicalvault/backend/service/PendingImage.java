package com.botanicalvault.backend.service;

public record PendingImage(
    byte[] content,
    String originalFilename,
    Long viewId
) {}

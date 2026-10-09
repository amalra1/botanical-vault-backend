package com.botanicalvault.backend.storage;

public interface ImageStorage {

    void save(String key, byte[] content);

    void delete(String key);

    String readUrl(String key);
}

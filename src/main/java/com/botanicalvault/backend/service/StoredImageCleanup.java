package com.botanicalvault.backend.service;

import java.util.List;

import com.botanicalvault.backend.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class StoredImageCleanup {
    private static final Logger log = LoggerFactory.getLogger(StoredImageCleanup.class);

    private final ImageStorage imageStorage;

    public void deleteOnRollback(List<String> storedKeys) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    storedKeys.forEach(StoredImageCleanup.this::deleteQuietly);
                }
            }
        });
    }

    private void deleteQuietly(String key) {
        try {
            imageStorage.delete(key);
        } catch (RuntimeException ex) {
            log.warn("Unable to delete orphan image {}", key, ex);
        }
    }
}

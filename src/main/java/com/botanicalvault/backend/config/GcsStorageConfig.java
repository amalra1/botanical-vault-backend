package com.botanicalvault.backend.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "storage.provider", havingValue = "gcs")
public class GcsStorageConfig {

    @Bean
    public Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }
}

package com.botanicalvault.backend.config;

import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.util.EmailAddresses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAccountProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        assertConfigured();
        User admin = User.builder()
                .email(EmailAddresses.normalize(properties.email()))
                .passwordHash(passwordEncoder.encode(properties.password()))
                .build();
        userRepository.save(admin);
        log.info("Admin account created for {}", admin.getEmail());
    }

    private void assertConfigured() {
        if (isBlank(properties.email()) || isBlank(properties.password())) {
            throw new IllegalStateException("APP_ADMIN_EMAIL and APP_ADMIN_PASSWORD must be set to create the admin account");
        }
        if (properties.password().length() < properties.minPasswordLength()) {
            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD must have at least " + properties.minPasswordLength() + " characters");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

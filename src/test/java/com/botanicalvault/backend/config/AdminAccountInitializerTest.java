package com.botanicalvault.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminAccountInitializerTest {
    private static final String PASSWORD = "test-password";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void createsAccountWithNormalizedEmailAndHashedPassword() {
        when(userRepository.count()).thenReturn(0L);

        initializer(" Architect@Test.local ", PASSWORD).run(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("architect@test.local");
        assertThat(passwordEncoder.matches(PASSWORD, saved.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void keepsExistingAccount() {
        when(userRepository.count()).thenReturn(1L);

        initializer("architect@test.local", PASSWORD).run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void failsWhenCredentialsAreMissing() {
        when(userRepository.count()).thenReturn(0L);

        assertThatThrownBy(() -> initializer("", "").run(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void failsWhenPasswordIsTooShort() {
        when(userRepository.count()).thenReturn(0L);

        assertThatThrownBy(() -> initializer("architect@test.local", "short").run(null))
                .isInstanceOf(IllegalStateException.class);
    }

    private AdminAccountInitializer initializer(String email, String password) {
        return new AdminAccountInitializer(userRepository, passwordEncoder, new AdminAccountProperties(email, password, MIN_PASSWORD_LENGTH));
    }
}

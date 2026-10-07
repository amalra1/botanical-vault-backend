package com.botanicalvault.backend.service;

import java.util.Optional;

import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.util.EmailAddresses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String email) {
        return findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(EmailAddresses.normalize(email));
    }
}

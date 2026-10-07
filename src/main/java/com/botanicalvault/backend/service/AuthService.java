package com.botanicalvault.backend.service;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.LoginRequestDTO;
import com.botanicalvault.backend.dto.response.LoginResponseDTO;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userDetailsService.findByEmail(dto.email())
                .filter(candidate -> passwordEncoder.matches(dto.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));
        IssuedToken issued = jwtService.issue(user.getEmail());
        return new LoginResponseDTO(issued.token(), issued.expiresAt());
    }
}

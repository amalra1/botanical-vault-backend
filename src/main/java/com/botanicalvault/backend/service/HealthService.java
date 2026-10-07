package com.botanicalvault.backend.service;

import com.botanicalvault.backend.dto.response.HealthResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthService {
    private static final String STATUS_UP = "UP";

    private final JdbcClient jdbcClient;

    public HealthResponseDTO check() {
        jdbcClient.sql("SELECT 1").query(Integer.class).single();
        return new HealthResponseDTO(STATUS_UP);
    }
}

package com.botanicalvault.backend.controller;

import com.botanicalvault.backend.dto.response.HealthResponseDTO;
import com.botanicalvault.backend.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {
    private final HealthService healthService;

    @GetMapping
    public ResponseEntity<HealthResponseDTO> health() {
        return ResponseEntity.ok(healthService.check());
    }
}

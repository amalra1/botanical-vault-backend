package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sizes")
@RequiredArgsConstructor
public class SizeController {
    private static final Logger log = LoggerFactory.getLogger(SizeController.class);
    private final SizeService sizeService;

    @GetMapping
    public ResponseEntity<List<SizeResponseDTO>> findAll() {
        log.info("GET /sizes");
        List<SizeResponseDTO> response = sizeService.findAll();
        log.info("GET /sizes - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }
}

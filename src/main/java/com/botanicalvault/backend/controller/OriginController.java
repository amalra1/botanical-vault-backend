package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.request.OriginCreateRequestDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.service.OriginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/origins")
@RequiredArgsConstructor
public class OriginController {
    private static final Logger log = LoggerFactory.getLogger(OriginController.class);
    private final OriginService originService;

    @GetMapping
    public ResponseEntity<List<TaxonomyItemResponseDTO>> findAll() {
        log.info("GET /origins");
        List<TaxonomyItemResponseDTO> response = originService.findAll();
        log.info("GET /origins - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TaxonomyItemResponseDTO> create(@Valid @RequestBody OriginCreateRequestDTO dto) {
        log.info("POST /origins - name: {}", dto.name());
        TaxonomyItemResponseDTO response = originService.create(dto);
        log.info("POST /origins - Success - ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

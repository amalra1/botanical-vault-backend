package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.service.OriginService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}

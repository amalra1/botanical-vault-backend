package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.service.PlantTypeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plant-types")
@RequiredArgsConstructor
public class PlantTypeController {
    private static final Logger log = LoggerFactory.getLogger(PlantTypeController.class);
    private final PlantTypeService plantTypeService;

    @GetMapping
    public ResponseEntity<List<TaxonomyItemResponseDTO>> findAll() {
        log.info("GET /plant-types");
        List<TaxonomyItemResponseDTO> response = plantTypeService.findAll();
        log.info("GET /plant-types - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }
}

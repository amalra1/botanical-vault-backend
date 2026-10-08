package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.request.TaxonomyNameRequestDTO;
import com.botanicalvault.backend.dto.request.TaxonomyOrderRequestDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyListItemResponseDTO;
import com.botanicalvault.backend.service.PlantTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plant-types")
@RequiredArgsConstructor
public class PlantTypeController {
    private static final Logger log = LoggerFactory.getLogger(PlantTypeController.class);
    private final PlantTypeService plantTypeService;

    @GetMapping
    public ResponseEntity<List<TaxonomyListItemResponseDTO>> findAll() {
        log.info("GET /plant-types");
        List<TaxonomyListItemResponseDTO> response = plantTypeService.findAll();
        log.info("GET /plant-types - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TaxonomyItemResponseDTO> create(@Valid @RequestBody TaxonomyNameRequestDTO dto) {
        log.info("POST /plant-types - name: {}", dto.name());
        TaxonomyItemResponseDTO response = plantTypeService.create(dto);
        log.info("POST /plant-types - Success - ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaxonomyItemResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TaxonomyNameRequestDTO dto) {
        log.info("PATCH /plant-types/{} - name: {}", id, dto.name());
        TaxonomyItemResponseDTO response = plantTypeService.update(id, dto);
        log.info("PATCH /plant-types/{} - Success", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody TaxonomyOrderRequestDTO dto) {
        log.info("PUT /plant-types/order - ids: {}", dto.ids());
        plantTypeService.reorder(dto);
        log.info("PUT /plant-types/order - Success");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(required = false) Long reassignTo) {
        log.info("DELETE /plant-types/{} - reassignTo: {}", id, reassignTo);
        plantTypeService.delete(id, reassignTo);
        log.info("DELETE /plant-types/{} - Success", id);
        return ResponseEntity.noContent().build();
    }
}

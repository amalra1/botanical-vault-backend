package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.request.TaxonomyNameRequestDTO;
import com.botanicalvault.backend.dto.request.TaxonomyOrderRequestDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyListItemResponseDTO;
import com.botanicalvault.backend.service.OriginService;
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
@RequestMapping("/origins")
@RequiredArgsConstructor
public class OriginController {
    private static final Logger log = LoggerFactory.getLogger(OriginController.class);
    private final OriginService originService;

    @GetMapping
    public ResponseEntity<List<TaxonomyListItemResponseDTO>> findAll() {
        log.info("GET /origins");
        List<TaxonomyListItemResponseDTO> response = originService.findAll();
        log.info("GET /origins - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TaxonomyItemResponseDTO> create(@Valid @RequestBody TaxonomyNameRequestDTO dto) {
        log.info("POST /origins - name: {}", dto.name());
        TaxonomyItemResponseDTO response = originService.create(dto);
        log.info("POST /origins - Success - ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaxonomyItemResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TaxonomyNameRequestDTO dto) {
        log.info("PATCH /origins/{} - name: {}", id, dto.name());
        TaxonomyItemResponseDTO response = originService.update(id, dto);
        log.info("PATCH /origins/{} - Success", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody TaxonomyOrderRequestDTO dto) {
        log.info("PUT /origins/order - ids: {}", dto.ids());
        originService.reorder(dto);
        log.info("PUT /origins/order - Success");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(required = false) Long reassignTo) {
        log.info("DELETE /origins/{} - reassignTo: {}", id, reassignTo);
        originService.delete(id, reassignTo);
        log.info("DELETE /origins/{} - Success", id);
        return ResponseEntity.noContent().build();
    }
}

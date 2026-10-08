package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.request.SizeRequestDTO;
import com.botanicalvault.backend.dto.request.TaxonomyOrderRequestDTO;
import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.dto.response.SizeListItemResponseDTO;
import com.botanicalvault.backend.service.SizeService;
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
@RequestMapping("/sizes")
@RequiredArgsConstructor
public class SizeController {
    private static final Logger log = LoggerFactory.getLogger(SizeController.class);
    private final SizeService sizeService;

    @GetMapping
    public ResponseEntity<List<SizeListItemResponseDTO>> findAll() {
        log.info("GET /sizes");
        List<SizeListItemResponseDTO> response = sizeService.findAll();
        log.info("GET /sizes - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SizeResponseDTO> create(@Valid @RequestBody SizeRequestDTO dto) {
        log.info("POST /sizes - name: {}", dto.name());
        SizeResponseDTO response = sizeService.create(dto);
        log.info("POST /sizes - Success - ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SizeResponseDTO> update(@PathVariable Long id, @Valid @RequestBody SizeRequestDTO dto) {
        log.info("PATCH /sizes/{} - name: {}", id, dto.name());
        SizeResponseDTO response = sizeService.update(id, dto);
        log.info("PATCH /sizes/{} - Success", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody TaxonomyOrderRequestDTO dto) {
        log.info("PUT /sizes/order - ids: {}", dto.ids());
        sizeService.reorder(dto);
        log.info("PUT /sizes/order - Success");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(required = false) Long reassignTo) {
        log.info("DELETE /sizes/{} - reassignTo: {}", id, reassignTo);
        sizeService.delete(id, reassignTo);
        log.info("DELETE /sizes/{} - Success", id);
        return ResponseEntity.noContent().build();
    }
}

package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.request.TaxonomyNameRequestDTO;
import com.botanicalvault.backend.dto.request.TaxonomyOrderRequestDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyListItemResponseDTO;
import com.botanicalvault.backend.service.ImageViewService;
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
@RequestMapping("/image-views")
@RequiredArgsConstructor
public class ImageViewController {
    private static final Logger log = LoggerFactory.getLogger(ImageViewController.class);
    private final ImageViewService imageViewService;

    @GetMapping
    public ResponseEntity<List<TaxonomyListItemResponseDTO>> findAll() {
        log.info("GET /image-views");
        List<TaxonomyListItemResponseDTO> response = imageViewService.findAll();
        log.info("GET /image-views - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TaxonomyItemResponseDTO> create(@Valid @RequestBody TaxonomyNameRequestDTO dto) {
        log.info("POST /image-views - name: {}", dto.name());
        TaxonomyItemResponseDTO response = imageViewService.create(dto);
        log.info("POST /image-views - Success - ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaxonomyItemResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TaxonomyNameRequestDTO dto) {
        log.info("PATCH /image-views/{} - name: {}", id, dto.name());
        TaxonomyItemResponseDTO response = imageViewService.update(id, dto);
        log.info("PATCH /image-views/{} - Success", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody TaxonomyOrderRequestDTO dto) {
        log.info("PUT /image-views/order - ids: {}", dto.ids());
        imageViewService.reorder(dto);
        log.info("PUT /image-views/order - Success");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(required = false) Long reassignTo) {
        log.info("DELETE /image-views/{} - reassignTo: {}", id, reassignTo);
        imageViewService.delete(id, reassignTo);
        log.info("DELETE /image-views/{} - Success", id);
        return ResponseEntity.noContent().build();
    }
}

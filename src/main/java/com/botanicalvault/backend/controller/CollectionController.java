package com.botanicalvault.backend.controller;

import java.net.URI;
import java.util.List;

import com.botanicalvault.backend.dto.request.CollectionNameRequestDTO;
import com.botanicalvault.backend.dto.response.CollectionImageResponseDTO;
import com.botanicalvault.backend.dto.response.CollectionResponseDTO;
import com.botanicalvault.backend.dto.response.CollectionSummaryResponseDTO;
import com.botanicalvault.backend.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {
    private static final Logger log = LoggerFactory.getLogger(CollectionController.class);
    private final CollectionService collectionService;

    @GetMapping
    public ResponseEntity<List<CollectionSummaryResponseDTO>> findAll() {
        log.info("GET /collections");
        List<CollectionSummaryResponseDTO> response = collectionService.findAll();
        log.info("GET /collections - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CollectionResponseDTO> create(@Valid @RequestBody CollectionNameRequestDTO dto) {
        log.info("POST /collections - name: {}", dto.name());
        CollectionResponseDTO response = collectionService.create(dto);
        log.info("POST /collections - Success - ID: {}", response.id());
        return ResponseEntity.created(URI.create("/collections/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> findById(@PathVariable Long id) {
        log.info("GET /collections/{}", id);
        CollectionResponseDTO response = collectionService.findById(id);
        log.info("GET /collections/{} - Success", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<CollectionImageResponseDTO>> findImages(@PathVariable Long id) {
        log.info("GET /collections/{}/images", id);
        List<CollectionImageResponseDTO> response = collectionService.findImages(id);
        log.info("GET /collections/{}/images - Success - count: {}", id, response.size());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> rename(
            @PathVariable Long id, @Valid @RequestBody CollectionNameRequestDTO dto) {
        log.info("PATCH /collections/{} - name: {}", id, dto.name());
        CollectionResponseDTO response = collectionService.rename(id, dto);
        log.info("PATCH /collections/{} - Success", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /collections/{}", id);
        collectionService.delete(id);
        log.info("DELETE /collections/{} - Success", id);
        return ResponseEntity.noContent().build();
    }
}

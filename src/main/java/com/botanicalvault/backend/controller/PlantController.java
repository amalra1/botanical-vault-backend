package com.botanicalvault.backend.controller;

import java.net.URI;
import java.util.List;

import com.botanicalvault.backend.dto.request.PlantCreateRequestDTO;
import com.botanicalvault.backend.dto.request.PlantSearchRequestDTO;
import com.botanicalvault.backend.dto.response.PlantResponseDTO;
import com.botanicalvault.backend.dto.response.PlantSearchResponseDTO;
import com.botanicalvault.backend.service.PlantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/plants")
@RequiredArgsConstructor
public class PlantController {
    private static final Logger log = LoggerFactory.getLogger(PlantController.class);
    private final PlantService plantService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlantResponseDTO> create(
            @Valid @RequestPart("plant") PlantCreateRequestDTO dto,
            @RequestPart("files") List<MultipartFile> files) {
        log.info("POST /plants - commonName: {}", dto.commonName());
        PlantResponseDTO response = plantService.create(dto, files);
        log.info("POST /plants - Success - ID: {}", response.id());
        return ResponseEntity.created(URI.create("/plants/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<PlantSearchResponseDTO> search(@Valid @ModelAttribute PlantSearchRequestDTO dto) {
        log.info("GET /plants - q: {}, typeId: {}, originId: {}, unclassified: {}, sizeId: {}, viewId: {}, sort: {}",
                dto.q(), dto.typeId(), dto.originId(), dto.unclassified(), dto.sizeId(), dto.viewId(), dto.sort());
        PlantSearchResponseDTO response = plantService.search(dto);
        log.info("GET /plants - Success - results: {}", response.plants().size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantResponseDTO> findById(@PathVariable Long id) {
        log.info("GET /plants/{}", id);
        PlantResponseDTO response = plantService.findById(id);
        log.info("GET /plants/{} - Success", id);
        return ResponseEntity.ok(response);
    }
}

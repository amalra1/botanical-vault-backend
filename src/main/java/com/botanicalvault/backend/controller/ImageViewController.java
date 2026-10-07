package com.botanicalvault.backend.controller;

import java.util.List;

import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.service.ImageViewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/image-views")
@RequiredArgsConstructor
public class ImageViewController {
    private static final Logger log = LoggerFactory.getLogger(ImageViewController.class);
    private final ImageViewService imageViewService;

    @GetMapping
    public ResponseEntity<List<TaxonomyItemResponseDTO>> findAll() {
        log.info("GET /image-views");
        List<TaxonomyItemResponseDTO> response = imageViewService.findAll();
        log.info("GET /image-views - Success - count: {}", response.size());
        return ResponseEntity.ok(response);
    }
}

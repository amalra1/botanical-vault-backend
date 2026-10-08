package com.botanicalvault.backend.controller;

import com.botanicalvault.backend.service.CollectionMembershipService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plants/{plantId}/favorite")
@RequiredArgsConstructor
public class PlantFavoriteController {
    private static final Logger log = LoggerFactory.getLogger(PlantFavoriteController.class);
    private final CollectionMembershipService membershipService;

    @PutMapping
    public ResponseEntity<Void> favorite(@PathVariable Long plantId) {
        log.info("PUT /plants/{}/favorite", plantId);
        membershipService.favorite(plantId);
        log.info("PUT /plants/{}/favorite - Success", plantId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unfavorite(@PathVariable Long plantId) {
        log.info("DELETE /plants/{}/favorite", plantId);
        membershipService.unfavorite(plantId);
        log.info("DELETE /plants/{}/favorite - Success", plantId);
        return ResponseEntity.noContent().build();
    }
}

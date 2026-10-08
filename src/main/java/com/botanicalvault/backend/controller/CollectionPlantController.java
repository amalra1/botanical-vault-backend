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
@RequestMapping("/collections/{collectionId}/plants/{plantId}")
@RequiredArgsConstructor
public class CollectionPlantController {
    private static final Logger log = LoggerFactory.getLogger(CollectionPlantController.class);
    private final CollectionMembershipService membershipService;

    @PutMapping
    public ResponseEntity<Void> add(@PathVariable Long collectionId, @PathVariable Long plantId) {
        log.info("PUT /collections/{}/plants/{}", collectionId, plantId);
        membershipService.addPlant(collectionId, plantId);
        log.info("PUT /collections/{}/plants/{} - Success", collectionId, plantId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@PathVariable Long collectionId, @PathVariable Long plantId) {
        log.info("DELETE /collections/{}/plants/{}", collectionId, plantId);
        membershipService.removePlant(collectionId, plantId);
        log.info("DELETE /collections/{}/plants/{} - Success", collectionId, plantId);
        return ResponseEntity.noContent().build();
    }
}

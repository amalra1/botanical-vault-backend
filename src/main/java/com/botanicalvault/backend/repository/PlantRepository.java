package com.botanicalvault.backend.repository;

import java.util.Optional;

import com.botanicalvault.backend.entity.Plant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant, Long> {

    @EntityGraph(attributePaths = {"type", "size", "origins", "images", "images.view"})
    Optional<Plant> findDetailedById(Long id);
}

package com.botanicalvault.backend.repository;

import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.entity.Plant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlantRepository extends JpaRepository<Plant, Long> {

    @EntityGraph(attributePaths = {"type", "size", "origins", "images", "images.view"})
    Optional<Plant> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"type", "size", "origins", "images", "images.view"})
    @Query("select p from Plant p")
    List<Plant> findAllDetailed();
}

package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.PlantType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantTypeRepository extends JpaRepository<PlantType, Long> {

    List<PlantType> findAllByOrderByPositionAsc();
}

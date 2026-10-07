package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.Origin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OriginRepository extends JpaRepository<Origin, Long> {

    List<Origin> findAllByOrderByPositionAsc();
}

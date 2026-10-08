package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.Origin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OriginRepository extends JpaRepository<Origin, Long> {

    List<Origin> findAllByOrderByPositionAsc();

    boolean existsByNameIgnoreCase(String name);

    @Query("select coalesce(max(o.position), 0) from Origin o")
    int findMaxPosition();
}

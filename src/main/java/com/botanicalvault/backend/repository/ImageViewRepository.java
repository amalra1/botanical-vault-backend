package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.ImageView;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ImageViewRepository extends TaxonomyRepository<ImageView> {

    @Override
    @Query("select v.id as id, count(i) as count from PlantImage i join i.view v group by v.id")
    List<UsageCount> countUsage();

    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = "update plant_images set view_id = :targetId where view_id = :sourceId")
    void reassignUsages(Long sourceId, Long targetId);
}

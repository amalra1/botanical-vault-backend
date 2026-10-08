package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.PlantType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlantTypeRepository extends TaxonomyRepository<PlantType> {

    @Override
    @Query("select t.id as id, count(p) as count from Plant p join p.type t group by t.id")
    List<UsageCount> countUsage();

    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = "update plants set type_id = :targetId where type_id = :sourceId")
    void reassignUsages(Long sourceId, Long targetId);
}

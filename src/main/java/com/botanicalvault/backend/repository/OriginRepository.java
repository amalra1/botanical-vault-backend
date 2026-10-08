package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.Origin;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OriginRepository extends TaxonomyRepository<Origin> {

    @Override
    @Query("select o.id as id, count(p) as count from Plant p join p.origins o group by o.id")
    List<UsageCount> countUsage();

    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
            insert into plant_origins (plant_id, origin_id)
            select plant_id, :targetId from plant_origins where origin_id = :sourceId
            on conflict do nothing""")
    void reassignUsages(Long sourceId, Long targetId);
}

package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.Size;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SizeRepository extends TaxonomyRepository<Size> {

    @Override
    @Query("select s.id as id, count(p) as count from Plant p join p.size s group by s.id")
    List<UsageCount> countUsage();

    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = "update plants set size_id = :targetId where size_id = :sourceId")
    void reassignUsages(Long sourceId, Long targetId);
}

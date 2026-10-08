package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.TaxonomyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaxonomyRepository<T extends TaxonomyItem> extends JpaRepository<T, Long> {

    List<T> findAllByOrderByPositionAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select coalesce(max(t.position), 0) from #{#entityName} t")
    int findMaxPosition();

    List<UsageCount> countUsage();

    void reassignUsages(Long sourceId, Long targetId);
}

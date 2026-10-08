package com.botanicalvault.backend.repository;

import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.entity.PlantCollection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlantCollectionRepository extends JpaRepository<PlantCollection, Long> {

    @EntityGraph(attributePaths = {"plants", "plants.type", "plants.size", "plants.origins", "plants.images"})
    @Query("select c from PlantCollection c order by c.favorites desc, c.updatedAt desc, c.id desc")
    List<PlantCollection> findAllDetailed();

    @EntityGraph(attributePaths = {"plants", "plants.type", "plants.size", "plants.origins", "plants.images"})
    Optional<PlantCollection> findDetailedById(Long id);

    Optional<PlantCollection> findByFavoritesTrue();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select p.id from PlantCollection c join c.plants p where c.favorites = true")
    List<Long> findFavoritePlantIds();

    @Query("select count(c) > 0 from PlantCollection c join c.plants p where c.favorites = true and p.id = :plantId")
    boolean isFavoritePlant(@Param("plantId") Long plantId);

    @Query("select c.id from PlantCollection c join c.plants p where p.id = :plantId order by c.id")
    List<Long> findIdsContainingPlant(@Param("plantId") Long plantId);
}

package com.botanicalvault.backend.entity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantCollection {
    private static final int NAME_MAX_LENGTH = 160;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(name = "is_favorites", nullable = false)
    private boolean favorites;

    @ManyToMany
    @JoinTable(
            name = "collection_plants",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "plant_id"))
    @Builder.Default
    private Set<Plant> plants = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean addPlant(Plant plant) {
        if (contains(plant.getId())) {
            return false;
        }
        return plants.add(plant);
    }

    public boolean removePlant(Long plantId) {
        return plants.removeIf(plant -> plant.getId().equals(plantId));
    }

    public void markUpdated(Instant now) {
        updatedAt = now;
    }

    private boolean contains(Long plantId) {
        return plants.stream().anyMatch(plant -> plant.getId().equals(plantId));
    }
}

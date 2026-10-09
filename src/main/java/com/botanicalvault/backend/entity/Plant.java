package com.botanicalvault.backend.entity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import com.botanicalvault.backend.constants.LightExposure;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "plants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plant {
    private static final int NAME_MAX_LENGTH = 160;
    private static final int LIGHT_EXPOSURE_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "common_name", nullable = false, length = NAME_MAX_LENGTH)
    private String commonName;

    @Column(name = "scientific_name", length = NAME_MAX_LENGTH)
    private String scientificName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private PlantType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    @ManyToMany
    @JoinTable(
            name = "plant_origins",
            joinColumns = @JoinColumn(name = "plant_id"),
            inverseJoinColumns = @JoinColumn(name = "origin_id"))
    @Builder.Default
    private Set<Origin> origins = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "plant_light_exposures", joinColumns = @JoinColumn(name = "plant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "exposure", nullable = false, length = LIGHT_EXPOSURE_MAX_LENGTH)
    @Builder.Default
    private Set<LightExposure> lightExposures = new LinkedHashSet<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    @Builder.Default
    private Set<PlantImage> images = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addImage(PlantImage image) {
        image.setPlant(this);
        images.add(image);
    }
}

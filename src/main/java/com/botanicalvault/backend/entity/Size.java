package com.botanicalvault.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "sizes")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Size extends TaxonomyItem {
    private static final int DESCRIPTION_MAX_LENGTH = 80;

    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;
}

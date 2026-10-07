package com.botanicalvault.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "image_views")
@NoArgsConstructor
@SuperBuilder
public class ImageView extends TaxonomyItem {
}

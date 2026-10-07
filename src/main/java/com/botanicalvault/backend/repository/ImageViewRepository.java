package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.ImageView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageViewRepository extends JpaRepository<ImageView, Long> {

    List<ImageView> findAllByOrderByPositionAsc();
}

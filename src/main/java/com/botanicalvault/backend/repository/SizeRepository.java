package com.botanicalvault.backend.repository;

import java.util.List;

import com.botanicalvault.backend.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SizeRepository extends JpaRepository<Size, Long> {

    List<Size> findAllByOrderByPositionAsc();
}

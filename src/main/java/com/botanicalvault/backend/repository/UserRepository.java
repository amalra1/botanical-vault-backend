package com.botanicalvault.backend.repository;

import java.util.Optional;

import com.botanicalvault.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}

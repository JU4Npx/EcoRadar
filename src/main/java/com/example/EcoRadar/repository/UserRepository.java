package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository
        extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
}
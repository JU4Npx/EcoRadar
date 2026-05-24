package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.GreenArea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreenAreaRepository
        extends JpaRepository<GreenArea, Integer> {
}
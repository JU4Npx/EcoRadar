package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.enums.GreenAreaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GreenAreaRepository
        extends JpaRepository<GreenArea, Integer> {

    List<GreenArea> findByStatus(GreenAreaStatus status);
}
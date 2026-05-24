package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.GreenAreaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreenAreaHistoryRepository
        extends JpaRepository<GreenAreaHistory, Integer> {
}
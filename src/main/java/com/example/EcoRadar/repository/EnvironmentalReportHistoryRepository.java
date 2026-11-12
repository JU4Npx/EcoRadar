package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.EnvironmentalReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvironmentalReportHistoryRepository extends JpaRepository<EnvironmentalReportHistory, Integer> {

    List<EnvironmentalReportHistory> findByReportIdOrderByCreatedAtDesc(Integer reportId);
}
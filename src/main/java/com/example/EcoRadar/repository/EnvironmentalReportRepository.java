package com.example.EcoRadar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.EcoRadar.model.entity.EnvironmentalReport;
import com.example.EcoRadar.model.enums.ReportCategory;
import java.time.LocalDateTime;

public interface EnvironmentalReportRepository extends JpaRepository<EnvironmentalReport, Integer> {

    List<EnvironmentalReport> findAllByOrderByCreatedAtDesc();

    Optional<EnvironmentalReport> findByTrackingCode(String trackingCode);

    List<EnvironmentalReport> findByCreatedAtBetweenOrderByCreatedAtAsc(LocalDateTime start, LocalDateTime end);

    List<EnvironmentalReport> findByCategoryAndCreatedAtAfterOrderByCreatedAtDesc(
            ReportCategory category, LocalDateTime createdAt);
}
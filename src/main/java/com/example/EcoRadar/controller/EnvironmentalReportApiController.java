package com.example.EcoRadar.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.EcoRadar.repository.EnvironmentalReportRepository;

@RestController
@RequestMapping("/api/environmental-reports")
public class EnvironmentalReportApiController {

    private final EnvironmentalReportRepository reportRepository;

    public EnvironmentalReportApiController(EnvironmentalReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/heatmap")
    public List<HeatPoint> heatmap() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(report -> report.getLatitude() != null && report.getLongitude() != null)
                .map(report -> new HeatPoint(
                        report.getLatitude().doubleValue(),
                        report.getLongitude().doubleValue(),
                        report.getCategory().name()))
                .toList();
    }

    public record HeatPoint(double latitude, double longitude, String category) {
    }
}
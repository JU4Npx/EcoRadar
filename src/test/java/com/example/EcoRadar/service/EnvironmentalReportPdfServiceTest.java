package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.EnvironmentalReport;
import com.example.EcoRadar.model.enums.ReportCategory;
import com.example.EcoRadar.repository.EnvironmentalReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvironmentalReportPdfServiceTest {

    @Mock
    private EnvironmentalReportRepository reportRepository;

    @Mock
    private GreenAreaService greenAreaService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EnvironmentalReportPdfService service;

    @Test
    void generatesPdfForSelectedDateRange() {
        EnvironmentalReport report = new EnvironmentalReport();
        report.setCategory(ReportCategory.LIXO);
        report.setRegionLabel("Centro");
        report.setCreatedAt(LocalDateTime.of(2026, 11, 3, 10, 0));
        when(reportRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any()))
                .thenReturn(List.of(report), List.of());
        when(greenAreaService.findAll()).thenReturn(List.of());
        when(eventService.findAll()).thenReturn(List.of());

        byte[] pdf = service.generate(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF"));
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generate(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1)));
    }
}

package com.example.EcoRadar.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.EcoRadar.model.entity.EnvironmentalReport;
import com.example.EcoRadar.model.entity.EnvironmentalReportHistory;
import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.model.enums.ReportCategory;
import com.example.EcoRadar.model.enums.ReportPriority;
import com.example.EcoRadar.model.enums.ReportStatus;
import com.example.EcoRadar.repository.EnvironmentalReportHistoryRepository;
import com.example.EcoRadar.repository.EnvironmentalReportRepository;
import com.example.EcoRadar.service.EventService;
import com.example.EcoRadar.service.GreenAreaService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MonitoringController {

    private final GreenAreaService greenAreaService;
    private final EventService eventService;
    private final EnvironmentalReportRepository reportRepository;
    private final EnvironmentalReportHistoryRepository historyRepository;

    public MonitoringController(GreenAreaService greenAreaService,
                                EventService eventService,
                                EnvironmentalReportRepository reportRepository,
                                EnvironmentalReportHistoryRepository historyRepository) {
        this.greenAreaService = greenAreaService;
        this.eventService = eventService;
        this.reportRepository = reportRepository;
        this.historyRepository = historyRepository;
    }

    @GetMapping("/monitoramento")
    public String dashboard(Model model) {
        List<GreenArea> activeAreas = greenAreaService.findAllActives();
        List<Event> upcomingEvents = eventService.findUpcomingEvents().stream()
                .filter(event -> event.getStatus() == null || !event.getStatus().name().equals("CANCELED"))
                .sorted(Comparator.comparing(Event::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<EnvironmentalReport> reports = reportRepository.findAllByOrderByCreatedAtDesc();
        reports.forEach(report -> {
            if (report.getPriority() == null) {
                report.setPriority(ReportPriority.MEDIUM);
            }
        });

        model.addAttribute("activeAreaCount", activeAreas.size());
        model.addAttribute("mappedAreaCount", activeAreas.stream()
                .filter(area -> area.getAddress() != null
                        && area.getAddress().getLatitude() != null
                        && area.getAddress().getLongitude() != null)
                .count());
        model.addAttribute("upcomingEventCount", upcomingEvents.size());
        model.addAttribute("upcomingEvents", upcomingEvents.stream().limit(5).toList());
        model.addAttribute("reportCount", reports.size());
        model.addAttribute("openReportCount", reports.stream()
                .filter(report -> report.getStatus() != ReportStatus.RESOLVED)
                .count());
        model.addAttribute("reports", reports.stream().limit(12).toList());
        model.addAttribute("categories", ReportCategory.values());
        model.addAttribute("reportStatuses", ReportStatus.values());
        return "monitoring/dashboard";
    }

    @GetMapping("/denuncias/acompanhar/{trackingCode}")
    public String trackReport(@PathVariable String trackingCode,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        return reportRepository.findByTrackingCode(trackingCode)
                .map(report -> {
                    model.addAttribute("report", report);
                    model.addAttribute("reportStatuses", ReportStatus.values());
                    return "monitoring/report-tracking";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Protocolo não encontrado.");
                    return "redirect:/monitoramento";
                });
    }

    @GetMapping("/denuncias/acompanhar")
    public String findReportByProtocol(@RequestParam String trackingCode) {
        return "redirect:/denuncias/acompanhar/" + trackingCode.trim();
    }

    @PostMapping("/denuncias")
    public String createReport(@RequestParam String title,
                               @RequestParam String description,
                               @RequestParam ReportCategory category,
                               @RequestParam(required = false) String regionLabel,
                               @RequestParam BigDecimal latitude,
                               @RequestParam BigDecimal longitude,
                               RedirectAttributes redirectAttributes) {
        String normalizedTitle = title.trim();
        String normalizedDescription = description.trim();
        if (normalizedTitle.length() < 5 || normalizedTitle.length() > 120
                || normalizedDescription.length() < 20 || normalizedDescription.length() > 3000
                || latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0
                || longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            redirectAttributes.addFlashAttribute("error", "Confira o título, a descrição e o ponto marcado no mapa.");
            return "redirect:/monitoramento#registrar";
        }

        EnvironmentalReport report = new EnvironmentalReport();
        report.setTitle(normalizedTitle);
        report.setDescription(normalizedDescription);
        report.setCategory(category);
        report.setRegionLabel(regionLabel == null || regionLabel.isBlank() ? null : regionLabel.trim());
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setStatus(ReportStatus.RECEIVED);
        report.setTrackingCode(UUID.randomUUID().toString());

        EnvironmentalReport possibleDuplicate = reportRepository
                .findByCategoryAndCreatedAtAfterOrderByCreatedAtDesc(category, LocalDateTime.now().minusDays(30))
                .stream()
                .filter(candidate -> candidate.getLatitude() != null && candidate.getLongitude() != null)
                .filter(candidate -> distanceMeters(latitude, longitude,
                        candidate.getLatitude(), candidate.getLongitude()) <= 150)
                .findFirst()
                .orElse(null);
        report.setDuplicateOf(possibleDuplicate);
        reportRepository.save(report);

        EnvironmentalReportHistory history = new EnvironmentalReportHistory();
        history.setReport(report);
        history.setAction(possibleDuplicate == null
                ? "Denúncia registrada"
                : "Possível duplicata detectada da denúncia #" + possibleDuplicate.getId());
        historyRepository.save(history);

        return "redirect:/denuncias/acompanhar/" + report.getTrackingCode();
    }

    @PostMapping("/monitoramento/denuncias/{id}/status")
    public String updateReportStatus(@PathVariable Integer id,
                                     @RequestParam ReportStatus status,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !user.hasPermission(Permission.MANAGE_USERS)) {
            return "redirect:/login";
        }

        reportRepository.findById(id).ifPresentOrElse(report -> {
            ReportStatus previousStatus = report.getStatus();
            report.setStatus(status);
            reportRepository.save(report);
            if (previousStatus != status) {
                EnvironmentalReportHistory history = new EnvironmentalReportHistory();
                history.setReport(report);
                history.setActor(user);
                history.setAction("Status: " + status.getLabel());
                historyRepository.save(history);
            }
            redirectAttributes.addFlashAttribute("message", "Status da denúncia atualizado.");
        }, () -> redirectAttributes.addFlashAttribute("error", "Denúncia não encontrada."));
        return "redirect:/monitoramento#denuncias";
    }

    private double distanceMeters(BigDecimal latitude1, BigDecimal longitude1,
                                  BigDecimal latitude2, BigDecimal longitude2) {
        double lat1 = Math.toRadians(latitude1.doubleValue());
        double lat2 = Math.toRadians(latitude2.doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(longitude2.doubleValue() - longitude1.doubleValue());
        double haversine = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        return 6_371_000 * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }
}

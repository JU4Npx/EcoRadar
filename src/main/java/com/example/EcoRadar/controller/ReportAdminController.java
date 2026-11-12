package com.example.EcoRadar.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.EcoRadar.model.entity.EnvironmentalReport;
import com.example.EcoRadar.model.entity.EnvironmentalReportHistory;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.model.enums.ReportCategory;
import com.example.EcoRadar.model.enums.ReportPriority;
import com.example.EcoRadar.model.enums.ReportStatus;
import com.example.EcoRadar.repository.EnvironmentalReportHistoryRepository;
import com.example.EcoRadar.repository.EnvironmentalReportRepository;
import com.example.EcoRadar.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
public class ReportAdminController {

    private final EnvironmentalReportRepository reportRepository;
    private final EnvironmentalReportHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public ReportAdminController(EnvironmentalReportRepository reportRepository,
                                 EnvironmentalReportHistoryRepository historyRepository,
                                 UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/denuncias")
    public String dashboard(@RequestParam(required = false) ReportStatus status,
                            @RequestParam(required = false) ReportCategory category,
                            @RequestParam(required = false) ReportPriority priority,
                            HttpSession session,
                            Model model) {
        if (!isManager(session)) {
            return "redirect:/login";
        }

        List<EnvironmentalReport> allReports = reportRepository.findAllByOrderByCreatedAtDesc();
        allReports.forEach(report -> {
            if (report.getPriority() == null) {
                report.setPriority(ReportPriority.MEDIUM);
            }
        });
        List<EnvironmentalReport> reports = allReports.stream()
                .filter(report -> status == null || report.getStatus() == status)
                .filter(report -> category == null || report.getCategory() == category)
                .filter(report -> priority == null || report.getPriority() == priority)
                .toList();
        Map<Integer, List<EnvironmentalReportHistory>> historyByReport = new LinkedHashMap<>();
        reports.forEach(report -> historyByReport.put(report.getId(),
                historyRepository.findByReportIdOrderByCreatedAtDesc(report.getId())));

        model.addAttribute("reports", reports);
        model.addAttribute("historyByReport", historyByReport);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("categories", ReportCategory.values());
        model.addAttribute("priorities", ReportPriority.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPriority", priority);
        return "monitoring/admin-reports";
    }

    @PostMapping("/admin/denuncias/{id}")
    @Transactional
    public String update(@PathVariable Integer id,
                         @RequestParam ReportCategory category,
                         @RequestParam ReportPriority priority,
                         @RequestParam ReportStatus status,
                         @RequestParam(required = false) Integer assignedUserId,
                         @RequestParam(required = false) Integer duplicateOfId,
                         @RequestParam(required = false) String note,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User actor = (User) session.getAttribute("loggedUser");
        if (actor == null || !actor.hasPermission(Permission.MANAGE_USERS)) {
            return "redirect:/login";
        }

        Optional<EnvironmentalReport> optionalReport = reportRepository.findById(id);
        if (optionalReport.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Denúncia não encontrada.");
            return "redirect:/admin/denuncias";
        }

        EnvironmentalReport report = optionalReport.get();
        User assignedUser = null;
        if (assignedUserId != null) {
            assignedUser = userRepository.findById(assignedUserId).orElse(null);
            if (assignedUser == null) {
                redirectAttributes.addFlashAttribute("error", "Responsável não encontrado.");
                return "redirect:/admin/denuncias";
            }
        }

        EnvironmentalReport duplicate = null;
        if (duplicateOfId != null) {
            if (duplicateOfId.equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Uma denúncia não pode ser duplicata de si mesma.");
                return "redirect:/admin/denuncias";
            }
            duplicate = reportRepository.findById(duplicateOfId).orElse(null);
            if (duplicate == null) {
                redirectAttributes.addFlashAttribute("error", "Denúncia relacionada não encontrada.");
                return "redirect:/admin/denuncias";
            }
        }

        List<String> changes = new ArrayList<>();
        if (report.getCategory() != category) {
            report.setCategory(category);
            changes.add("Categoria: " + category.getLabel());
        }
        if (report.getPriority() != priority) {
            report.setPriority(priority);
            changes.add("Prioridade: " + priority.getLabel());
        }
        if (report.getStatus() != status) {
            report.setStatus(status);
            changes.add("Status: " + status.getLabel());
        }

        Integer previousAssigneeId = report.getAssignedTo() == null ? null : report.getAssignedTo().getId();
        if (!java.util.Objects.equals(previousAssigneeId, assignedUserId)) {
            report.setAssignedTo(assignedUser);
            changes.add("Responsável: " + (assignedUser == null ? "não atribuído" : assignedUser.getName()));
        }

        Integer previousDuplicateId = report.getDuplicateOf() == null ? null : report.getDuplicateOf().getId();
        if (!java.util.Objects.equals(previousDuplicateId, duplicateOfId)) {
            report.setDuplicateOf(duplicate);
            changes.add("Duplicidade: " + (duplicate == null ? "removida" : "relacionada à denúncia #" + duplicateOfId));
        }

        reportRepository.save(report);
        String normalizedNote = note == null ? "" : note.trim();
        if (!normalizedNote.isBlank() && normalizedNote.length() > 900) {
            normalizedNote = normalizedNote.substring(0, 900);
        }
        if (!normalizedNote.isBlank()) {
            changes.add("Nota: " + normalizedNote);
        }
        if (!changes.isEmpty()) {
            EnvironmentalReportHistory history = new EnvironmentalReportHistory();
            history.setReport(report);
            history.setActor(actor);
            history.setAction(String.join(" | ", changes));
            historyRepository.save(history);
        }

        redirectAttributes.addFlashAttribute("message", "Atendimento da denúncia atualizado.");
        return "redirect:/admin/denuncias#denuncia-" + id;
    }

    private boolean isManager(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        return user != null && user.hasPermission(Permission.MANAGE_USERS);
    }
}
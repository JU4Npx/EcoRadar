package com.example.EcoRadar.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.service.EnvironmentalReportPdfService;

import jakarta.servlet.http.HttpSession;

@Controller
public class EnvironmentalReportAnalyticsController {

    private final EnvironmentalReportPdfService pdfService;

    public EnvironmentalReportAnalyticsController(EnvironmentalReportPdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/admin/relatorios")
    public String reports(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                          HttpSession session,
                          Model model) {
        if (!isManager(session)) {
            return "redirect:/login";
        }
        LocalDate end = fim == null ? LocalDate.now() : fim;
        LocalDate start = inicio == null ? YearMonth.from(end).atDay(1) : inicio;
        model.addAttribute("inicio", start);
        model.addAttribute("fim", end);
        model.addAttribute("error", end.isBefore(start) ? "A data final deve ser igual ou posterior à inicial." : null);
        return "monitoring/analytics";
    }

    @GetMapping("/admin/relatorios/pdf")
    public ResponseEntity<byte[]> download(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                                           HttpSession session) {
        if (!isManager(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (fim.isBefore(inicio)) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdf = pdfService.generate(inicio, fim);
        String filename = "ecoradar-relatorio-" + inicio.format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + fim.format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    private boolean isManager(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        return user != null && user.hasPermission(Permission.MANAGE_USERS);
    }
}
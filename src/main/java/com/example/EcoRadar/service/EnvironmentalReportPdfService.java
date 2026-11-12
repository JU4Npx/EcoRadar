package com.example.EcoRadar.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.EcoRadar.model.enums.EventStatus;
import com.example.EcoRadar.repository.EnvironmentalReportRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class EnvironmentalReportPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("pt-BR"));

    private final EnvironmentalReportRepository reportRepository;
    private final GreenAreaService greenAreaService;
    private final EventService eventService;

    public EnvironmentalReportPdfService(EnvironmentalReportRepository reportRepository,
                                         GreenAreaService greenAreaService,
                                         EventService eventService) {
        this.reportRepository = reportRepository;
        this.greenAreaService = greenAreaService;
        this.eventService = eventService;
    }

    public byte[] generate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Informe um intervalo de datas válido.");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        var reports = reportRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(start, end);
        var previousYearReports = reportRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(
                start.minusYears(1), end.minusYears(1));
        long completedEvents = eventService.findAll().stream()
                .filter(event -> event.getStatus() == EventStatus.COMPLETED)
                .filter(event -> event.getStartDate() != null
                        && !event.getStartDate().isBefore(start)
                        && !event.getStartDate().isAfter(end))
                .count();
        Map<String, Long> regions = reports.stream()
                .map(report -> report.getRegionLabel() == null || report.getRegionLabel().isBlank()
                        ? "Localidade não informada" : report.getRegionLabel().trim())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<YearMonth, Long> months = reports.stream()
                .filter(report -> report.getCreatedAt() != null)
                .collect(Collectors.groupingBy(report -> YearMonth.from(report.getCreatedAt()),
                        TreeMap::new, Collectors.counting()));
        Map<String, Long> categories = reports.stream()
                .collect(Collectors.groupingBy(report -> report.getCategory().getLabel(),
                        Collectors.counting()));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 42, 42, 42, 42);
        try {
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(18, 56, 40));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.BOLD, new Color(31, 107, 73));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, new Color(54, 72, 63));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.BOLD, Color.WHITE);

            document.add(new Paragraph("Relatório de monitoramento ambiental", titleFont));
            document.add(new Paragraph("Período: " + DATE_FORMAT.format(startDate) + " a " + DATE_FORMAT.format(endDate), bodyFont));
            document.add(new Paragraph("Gerado em: " + DATE_FORMAT.format(LocalDate.now()), bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Indicadores do período", sectionFont));
            PdfPTable metrics = new PdfPTable(4);
            metrics.setWidthPercentage(100);
            addMetric(metrics, "Denúncias", String.valueOf(reports.size()), headerFont, bodyFont);
            addMetric(metrics, "Áreas cadastradas", String.valueOf(greenAreaService.findAll().size()), headerFont, bodyFont);
            addMetric(metrics, "Eventos realizados", String.valueOf(completedEvents), headerFont, bodyFont);
            addMetric(metrics, "Regiões reportadas", String.valueOf(regions.size()), headerFont, bodyFont);
            document.add(metrics);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Comparação anual", sectionFont));
            PdfPTable yearly = createTable(new String[]{"Período", "Denúncias"}, headerFont);
            addRow(yearly, DATE_FORMAT.format(startDate) + " a " + DATE_FORMAT.format(endDate), reports.size(), bodyFont);
            addRow(yearly, DATE_FORMAT.format(startDate.minusYears(1)) + " a " + DATE_FORMAT.format(endDate.minusYears(1)), previousYearReports.size(), bodyFont);
            long difference = reports.size() - previousYearReports.size();
            addRow(yearly, "Variação absoluta", (difference > 0 ? "+" : "") + difference, bodyFont);
            String percentage = previousYearReports.isEmpty()
                    ? (reports.isEmpty() ? "0%" : "Sem base de comparação")
                    : String.format(java.util.Locale.forLanguageTag("pt-BR"), "%+.1f%%",
                    difference * 100.0 / previousYearReports.size());
            addRow(yearly, "Variação percentual", percentage, bodyFont);
            document.add(yearly);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Denúncias por mês", sectionFont));
            PdfPTable monthly = createTable(new String[]{"Mês", "Denúncias"}, headerFont);
            if (months.isEmpty()) {
                addRow(monthly, "Sem registros no período", 0, bodyFont);
            } else {
                months.forEach((month, count) -> addRow(monthly, MONTH_FORMAT.format(month.atDay(1)), count, bodyFont));
            }
            document.add(monthly);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Regiões com mais ocorrências", sectionFont));
            PdfPTable regionTable = createTable(new String[]{"Bairro ou região", "Denúncias"}, headerFont);
            regions.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                            .thenComparing(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)))
                    .limit(10)
                    .forEach(entry -> addRow(regionTable, entry.getKey(), entry.getValue(), bodyFont));
            if (regions.isEmpty()) {
                addRow(regionTable, "Sem registros no período", 0, bodyFont);
            }
            document.add(regionTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Denúncias por categoria", sectionFont));
            PdfPTable categoryTable = createTable(new String[]{"Categoria", "Denúncias"}, headerFont);
            categories.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> addRow(categoryTable, entry.getKey(), entry.getValue(), bodyFont));
            if (categories.isEmpty()) {
                addRow(categoryTable, "Sem registros no período", 0, bodyFont);
            }
            document.add(categoryTable);
        } catch (DocumentException exception) {
            throw new IllegalStateException("Não foi possível gerar o relatório PDF.", exception);
        } finally {
            document.close();
        }
        return output.toByteArray();
    }

    private void addMetric(PdfPTable table, String label, String value, Font headerFont, Font bodyFont) {
        var header = new com.lowagie.text.pdf.PdfPCell(new Phrase(label, headerFont));
        header.setBackgroundColor(new Color(31, 107, 73));
        header.setPadding(6);
        table.addCell(header);
        var content = new com.lowagie.text.pdf.PdfPCell(new Phrase(value, bodyFont));
        content.setPadding(7);
        table.addCell(content);
    }

    private PdfPTable createTable(String[] headers, Font headerFont) throws DocumentException {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        for (String headerText : headers) {
            var header = new com.lowagie.text.pdf.PdfPCell(new Phrase(headerText, headerFont));
            header.setBackgroundColor(new Color(31, 107, 73));
            header.setPadding(6);
            table.addCell(header);
        }
        return table;
    }

    private void addRow(PdfPTable table, String label, Object value, Font bodyFont) {
        var labelCell = new com.lowagie.text.pdf.PdfPCell(new Phrase(label, bodyFont));
        labelCell.setPadding(6);
        table.addCell(labelCell);
        var valueCell = new com.lowagie.text.pdf.PdfPCell(new Phrase(String.valueOf(value), bodyFont));
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }
}
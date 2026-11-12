package com.example.EcoRadar.controller;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.EcoRadar.model.entity.EnvironmentalReport;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.ReportCategory;
import com.example.EcoRadar.model.enums.ReportStatus;
import com.example.EcoRadar.repository.EnvironmentalReportHistoryRepository;
import com.example.EcoRadar.repository.EnvironmentalReportRepository;
import com.example.EcoRadar.service.EventService;
import com.example.EcoRadar.service.GreenAreaService;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    @Mock
    private GreenAreaService greenAreaService;

    @Mock
    private EventService eventService;

    @Mock
    private EnvironmentalReportRepository reportRepository;

    @Mock
    private EnvironmentalReportHistoryRepository historyRepository;

    @InjectMocks
    private MonitoringController controller;

    @Test
    void createsReportAndRedirectsToItsTrackingProtocol() {
        when(reportRepository.save(any(EnvironmentalReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String result = controller.createReport(
                "Descarte irregular",
                "Entulho acumulado ao lado da entrada principal do parque.",
                ReportCategory.LIXO,
                "Pajuçara",
                new BigDecimal("-9.6500000"),
                new BigDecimal("-35.7300000"),
                new RedirectAttributesModelMap());

        var savedReport = org.mockito.ArgumentCaptor.forClass(EnvironmentalReport.class);
        verify(reportRepository).save(savedReport.capture());
        EnvironmentalReport report = savedReport.getValue();
        assertEquals(ReportStatus.RECEIVED, report.getStatus());
        assertEquals(ReportCategory.LIXO, report.getCategory());
        assertEquals("Pajuçara", report.getRegionLabel());
        assertNotNull(report.getTrackingCode());
        assertTrue(result.endsWith(report.getTrackingCode()));
        assertEquals(report.getTrackingCode(), UUID.fromString(report.getTrackingCode()).toString());
        verify(historyRepository).save(any());
    }

    @Test
    void rejectsCoordinatesOutsideTheValidRange() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String result = controller.createReport(
                "Descarte irregular",
                "Entulho acumulado ao lado da entrada principal do parque.",
                ReportCategory.LIXO,
                null,
                new BigDecimal("-9.6500000"),
                new BigDecimal("-200"),
                redirectAttributes);

        assertEquals("redirect:/monitoramento#registrar", result);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        verify(reportRepository, never()).save(any(EnvironmentalReport.class));
    }

    @Test
    void findsReportByTrackingProtocolAndPopulatesTrackingView() {
        EnvironmentalReport report = new EnvironmentalReport();
        ExtendedModelMap model = new ExtendedModelMap();
        when(reportRepository.findByTrackingCode("protocol")).thenReturn(Optional.of(report));

        String result = controller.trackReport("protocol", model, new RedirectAttributesModelMap());

        assertEquals("monitoring/report-tracking", result);
        assertEquals(report, model.get("report"));
        assertNotNull(model.get("reportStatuses"));
    }

    @Test
    void redirectsProtocolLookupToTheTrackingPage() {
        assertEquals("redirect:/denuncias/acompanhar/protocol-code",
                controller.findReportByProtocol("  protocol-code  "));
    }

    @Test
    void redirectsWhenTrackingProtocolDoesNotExist() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        when(reportRepository.findByTrackingCode("missing")).thenReturn(Optional.empty());

        String result = controller.trackReport("missing", new ExtendedModelMap(), redirectAttributes);

        assertEquals("redirect:/monitoramento", result);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }

    @Test
    void onlyManagersCanUpdateReportStatus() {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setId(1);
        session.setAttribute("loggedUser", user);
        EnvironmentalReport report = new EnvironmentalReport();
        when(reportRepository.findById(3)).thenReturn(Optional.of(report));

        String result = controller.updateReportStatus(
                3, ReportStatus.RESOLVED, session, new RedirectAttributesModelMap());

        assertEquals("redirect:/monitoramento#denuncias", result);
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        verify(reportRepository).save(report);
    }

    @Test
    void rejectsStatusUpdatesFromRegularUsers() {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setId(2);
        session.setAttribute("loggedUser", user);

        String result = controller.updateReportStatus(
                3, ReportStatus.RESOLVED, session, new RedirectAttributesModelMap());

        assertEquals("redirect:/login", result);
        verifyNoInteractions(reportRepository);
    }
}

package com.example.EcoRadar.controller;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.EcoRadar.model.entity.EnvironmentalReport;
import com.example.EcoRadar.model.entity.EnvironmentalReportHistory;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.ReportCategory;
import com.example.EcoRadar.model.enums.ReportPriority;
import com.example.EcoRadar.model.enums.ReportStatus;
import com.example.EcoRadar.repository.EnvironmentalReportHistoryRepository;
import com.example.EcoRadar.repository.EnvironmentalReportRepository;
import com.example.EcoRadar.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReportAdminControllerTest {

    @Mock
    private EnvironmentalReportRepository reportRepository;

    @Mock
    private EnvironmentalReportHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportAdminController controller;

    @Test
    void updatesClassificationAssignmentDuplicateAndAuditHistory() {
        MockHttpSession session = new MockHttpSession();
        User manager = new User();
        manager.setId(1);
        session.setAttribute("loggedUser", manager);

        EnvironmentalReport report = new EnvironmentalReport();
        report.setCategory(ReportCategory.LIXO);
        report.setPriority(ReportPriority.LOW);
        report.setStatus(ReportStatus.RECEIVED);
        EnvironmentalReport original = new EnvironmentalReport();
        User assignee = new User();
        assignee.setId(9);
        assignee.setName("Equipe ambiental");
        when(reportRepository.findById(5)).thenReturn(Optional.of(report));
        when(reportRepository.findById(6)).thenReturn(Optional.of(original));
        when(userRepository.findById(9)).thenReturn(Optional.of(assignee));

        String result = controller.update(5, ReportCategory.AGUA, ReportPriority.HIGH,
                ReportStatus.IN_PROGRESS, 9, 6, "Vistoria agendada", session,
                new RedirectAttributesModelMap());

        assertEquals("redirect:/admin/denuncias#denuncia-5", result);
        assertEquals(ReportCategory.AGUA, report.getCategory());
        assertEquals(ReportPriority.HIGH, report.getPriority());
        assertEquals(ReportStatus.IN_PROGRESS, report.getStatus());
        assertEquals(assignee, report.getAssignedTo());
        assertEquals(original, report.getDuplicateOf());
        verify(reportRepository).save(report);
        ArgumentCaptor<EnvironmentalReportHistory> history = ArgumentCaptor.forClass(EnvironmentalReportHistory.class);
        verify(historyRepository).save(history.capture());
        assertEquals(manager, history.getValue().getActor());
        assertNotNull(history.getValue().getAction());
    }

    @Test
    void blocksAdministrativeUpdatesForRegularUsers() {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setId(2);
        session.setAttribute("loggedUser", user);

        String result = controller.update(5, ReportCategory.LIXO, ReportPriority.LOW,
                ReportStatus.RECEIVED, null, null, null, session,
                new RedirectAttributesModelMap());

        assertEquals("redirect:/login", result);
        verifyNoInteractions(reportRepository, historyRepository, userRepository);
    }

    @Test
    void doesNotPersistPartialChangesWhenAssigneeIsInvalid() {
        MockHttpSession session = new MockHttpSession();
        User manager = new User();
        manager.setId(1);
        session.setAttribute("loggedUser", manager);
        EnvironmentalReport report = new EnvironmentalReport();
        report.setCategory(ReportCategory.LIXO);
        report.setPriority(ReportPriority.LOW);
        report.setStatus(ReportStatus.RECEIVED);
        when(reportRepository.findById(5)).thenReturn(Optional.of(report));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        String result = controller.update(5, ReportCategory.AGUA, ReportPriority.HIGH,
                ReportStatus.IN_PROGRESS, 99, null, null, session,
                new RedirectAttributesModelMap());

        assertEquals("redirect:/admin/denuncias", result);
        assertEquals(ReportCategory.LIXO, report.getCategory());
        assertEquals(ReportPriority.LOW, report.getPriority());
        assertEquals(ReportStatus.RECEIVED, report.getStatus());
        verifyNoInteractions(historyRepository);
    }
}

package com.atome.bot.service;

import com.atome.bot.model.Report;
import com.atome.bot.repositories.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportsServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportsService service;

    @Test
    void create_buildsOpenReportAndSavesIt() {
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        Report result = service.create("q", "a", "feedback", "expected");

        assertNotNull(result.getId());
        assertEquals("OPEN", result.getStatus());
        assertEquals("q", result.getQuestion());
        assertEquals("a", result.getBotAnswer());
        assertNotNull(result.getCreatedAt());
        assertNull(result.getResolvedAt());
    }

    @Test
    void listOpen_returnsRepositoryResult() {
        List<Report> expected = List.of(new Report());
        when(reportRepository.findByStatusOrderByCreatedAtDesc("OPEN")).thenReturn(expected);

        assertSame(expected, service.listOpen());
    }

    @Test
    void markFixed_setsStatusAndResolvedTime() {
        Report report = new Report("r1", "q", "a", "f", "e", "OPEN", null, null);
        when(reportRepository.findById("r1")).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        Report result = service.markFixed("r1");

        assertEquals("FIXED", result.getStatus());
        assertNotNull(result.getResolvedAt());
    }

    @Test
    void archive_setsStatusAndResolvedTime() {
        Report report = new Report("r1", "q", "a", "f", "e", "OPEN", null, null);
        when(reportRepository.findById("r1")).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        Report result = service.archive("r1");

        assertEquals("ARCHIVED", result.getStatus());
        assertNotNull(result.getResolvedAt());
    }

    @Test
    void get_returnsReportWhenPresent() {
        Report report = new Report();
        when(reportRepository.findById("r1")).thenReturn(Optional.of(report));

        assertSame(report, service.get("r1"));
    }

    @Test
    void get_throwsWhenMissing() {
        when(reportRepository.findById("r1")).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> service.get("r1"));
    }
}

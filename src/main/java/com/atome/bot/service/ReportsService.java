package com.atome.bot.service;

import com.atome.bot.model.Report;
import com.atome.bot.repositories.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReportsService {
    private final ReportRepository reportRepository;

    public ReportsService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report create(String question, String botAnswer, String feedback, String expected) {
        Report r = new Report(
                UUID.randomUUID().toString(),
                question,
                botAnswer,
                feedback,
                expected,
                "OPEN",
                Instant.now(),
                null
        );
        return reportRepository.save(r);
    }

    public List<Report> listOpen() {
        return reportRepository.findByStatusOrderByCreatedAtDesc("OPEN");
    }

    public Report markFixed(String id) {
        Report r = reportRepository.findById(id).orElseThrow();
        r.setStatus("FIXED");
        r.setResolvedAt(Instant.now());
        return reportRepository.save(r);
    }

    public Report archive(String id) {
        Report r = reportRepository.findById(id).orElseThrow();
        r.setStatus("ARCHIVED");
        r.setResolvedAt(Instant.now());
        return reportRepository.save(r);
    }

    public Report get(String id) {
        return reportRepository.findById(id).orElseThrow();
    }

}

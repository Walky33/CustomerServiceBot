package com.atome.bot.repositories;

import com.atome.bot.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report,String> {
    List<Report> findByStatusOrderByCreatedAtDesc(String status);
}

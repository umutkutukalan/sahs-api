package com.sahnesen.api.sahnesen.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.Report;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.ReportType;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // Aynı kullanıcı aynı içeriği daha önce raporlamış mı kontrolü için:
    boolean existsByReporterAndTargetIdAndReportType(User reporter, Long targetId, ReportType reportType);
}

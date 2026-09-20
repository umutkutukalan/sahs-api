package com.sahnesen.api.sahnesen.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.entities.Report;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.ReportRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.request.ReportRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createReport(ReportRequest request, String currentUsername) {
        // 1. İstek atan kullanıcıyı bul
        User reporter = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        // 2. Aynı kullanıcı aynı içeriği tekrar raporlamış mı kontrol et (Spam önleme)
        boolean alreadyReported = reportRepository.existsByReporterAndTargetIdAndReportType(
                reporter, request.getTargetId(), request.getReportType());

        if (alreadyReported) {
            throw new RuntimeException("Bu içeriği zaten raporladınız.");
        }

        // 3. Rapor nesnesini oluştur ve kaydet
        Report report = Report.builder()
                .reporter(reporter)
                .targetId(request.getTargetId())
                .reportType(request.getReportType())
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
    }
}

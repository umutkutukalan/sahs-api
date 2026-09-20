package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.request.ReportRequest;
import com.sahnesen.api.sahnesen.services.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> createReport(@RequestBody ReportRequest request, Principal principal) {
        // Principal sayesinde JWT veya Security context üzerinden giriş yapan
        // kullanıcıyı alıyoruz
        if (principal == null) {
            return ResponseEntity.status(401).body("Bu işlem için giriş yapmalısınız.");
        }

        reportService.createReport(request, principal.getName());

        return ResponseEntity.ok("İçerik başarıyla raporlandı. İncelemeye alınacaktır.");
    }
}
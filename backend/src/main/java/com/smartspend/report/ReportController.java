package com.smartspend.report;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartspend.report.dtos.ReportResponseDTO;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    @GetMapping("/report-data")
    public ResponseEntity<ReportResponseDTO> getReportData(
            @RequestParam Long bankAccountId,
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            ReportResponseDTO reportData = reportService.getResponseData(bankAccountId, userEmail, year, month);
            return ResponseEntity.ok(reportData);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

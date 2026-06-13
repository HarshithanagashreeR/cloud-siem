package com.amazon.siem.controller;

import com.amazon.siem.model.Report;
import com.amazon.siem.model.User;
import com.amazon.siem.repository.UserRepository;
import com.amazon.siem.service.report.ReportingService;
import com.amazon.siem.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportingService.getAllReports());
    }

    @PostMapping("/incident/{incidentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALYST', 'ROLE_ADMIN')")
    public ResponseEntity<Report> generateIncidentReport(@PathVariable UUID incidentId, Principal principal, HttpServletRequest request) {
        String username = principal.getName();
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in analyst not found."));

        Report report = reportingService.generateIncidentReportPdf(incidentId, creator);

        auditLogService.logAction(
                username,
                "REPORT_GENERATE",
                "Report",
                "SUCCESS",
                request.getRemoteAddr(),
                "Generated PDF incident report for incident " + incidentId
        );

        return ResponseEntity.ok(report);
    }

    @PostMapping("/system")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALYST', 'ROLE_ADMIN')")
    public ResponseEntity<Report> generateSystemReport(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            Principal principal,
            HttpServletRequest request) {
        
        String username = principal.getName();
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in analyst not found."));

        LocalDateTime startTime = start != null ? LocalDateTime.parse(start) : LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = end != null ? LocalDateTime.parse(end) : LocalDateTime.now();

        Report report = reportingService.generateSystemSecurityReportPdf(startTime, endTime, creator);

        auditLogService.logAction(
                username,
                "REPORT_GENERATE",
                "Report",
                "SUCCESS",
                request.getRemoteAddr(),
                "Generated PDF system posture metrics report."
        );

        return ResponseEntity.ok(report);
    }
}

package com.amazon.siem.controller;

import com.amazon.siem.dto.AiReportResponseDto;
import com.amazon.siem.model.Incident;
import com.amazon.siem.model.User;
import com.amazon.siem.repository.UserRepository;
import com.amazon.siem.service.ai.AiSecurityAnalystService;
import com.amazon.siem.service.alert.IncidentService;
import com.amazon.siem.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiSecurityAnalystService aiSecurityAnalystService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<Incident>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incident> getIncidentById(@PathVariable UUID id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALYST', 'ROLE_ADMIN')")
    public ResponseEntity<?> assignAnalyst(@PathVariable UUID id, Principal principal, HttpServletRequest request) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated analyst not found."));

        Incident incident = incidentService.assignAnalyst(id, user);

        auditLogService.logAction(
                username,
                "INCIDENT_ASSIGN",
                "Incident",
                "SUCCESS",
                request.getRemoteAddr(),
                "Assigned analyst " + username + " to incident: " + id
        );

        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALYST', 'ROLE_ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestParam String status, Principal principal, HttpServletRequest request) {
        String username = principal.getName();
        Incident incident = incidentService.updateStatus(id, status);

        auditLogService.logAction(
                username,
                "INCIDENT_STATUS_UPDATE",
                "Incident",
                "SUCCESS",
                request.getRemoteAddr(),
                "Updated incident " + id + " status to " + status
        );

        return ResponseEntity.ok(incident);
    }

    @PostMapping("/{id}/analyze")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALYST', 'ROLE_ADMIN')")
    public ResponseEntity<AiReportResponseDto> analyzeIncident(@PathVariable UUID id, Principal principal, HttpServletRequest request) {
        AiReportResponseDto report = aiSecurityAnalystService.analyzeIncident(id);

        auditLogService.logAction(
                principal.getName(),
                "INCIDENT_AI_ANALYZE",
                "Incident",
                "SUCCESS",
                request.getRemoteAddr(),
                "Triggered AI Security Analyst for incident: " + id
        );

        return ResponseEntity.ok(report);
    }
}

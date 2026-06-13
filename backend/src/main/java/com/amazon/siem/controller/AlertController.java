package com.amazon.siem.controller;

import com.amazon.siem.model.Alert;
import com.amazon.siem.model.User;
import com.amazon.siem.repository.UserRepository;
import com.amazon.siem.service.alert.AlertService;
import com.amazon.siem.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALYST', 'ROLE_ADMIN')")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable UUID id, Principal principal, HttpServletRequest request) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in analyst not found."));

        Alert alert = alertService.acknowledgeAlert(id, user);

        auditLogService.logAction(
                username,
                "ALERT_ACKNOWLEDGE",
                "Alert",
                "SUCCESS",
                request.getRemoteAddr(),
                "Acknowledged alert: " + id
        );

        return ResponseEntity.ok(alert);
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALYST', 'ROLE_ADMIN')")
    public ResponseEntity<?> resolveAlert(@PathVariable UUID id, Principal principal, HttpServletRequest request) {
        String username = principal.getName();
        Alert alert = alertService.resolveAlert(id);

        auditLogService.logAction(
                username,
                "ALERT_RESOLVE",
                "Alert",
                "SUCCESS",
                request.getRemoteAddr(),
                "Resolved alert: " + id
        );

        return ResponseEntity.ok(alert);
    }
}

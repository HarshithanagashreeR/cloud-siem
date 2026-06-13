package com.amazon.siem.controller;

import com.amazon.siem.dto.DashboardMetricsDto;
import com.amazon.siem.repository.AlertRepository;
import com.amazon.siem.repository.IncidentRepository;
import com.amazon.siem.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDto> getDashboardMetrics() {
        long totalLogs = logRepository.countAllLogs();
        long activeIncidents = incidentRepository.countActiveIncidents();
        long criticalAlerts = alertRepository.countCriticalActiveAlerts();

        // 1. Group logs by event type
        Map<String, Long> logsByType = new HashMap<>();
        List<Object[]> eventTypeCounts = logRepository.countLogsGroupedByEventType();
        for (Object[] row : eventTypeCounts) {
            if (row[0] != null) {
                logsByType.put(row[0].toString(), (Long) row[1]);
            }
        }

        // 2. Group alerts by severity
        Map<String, Long> alertsBySev = new HashMap<>();
        List<Object[]> alertCounts = alertRepository.countAlertsGroupedBySeverity();
        for (Object[] row : alertCounts) {
            if (row[0] != null) {
                alertsBySev.put(row[0].toString(), (Long) row[1]);
            }
        }

        // 3. Group incidents by severity
        Map<String, Long> incidentsBySev = new HashMap<>();
        List<Object[]> incidentCounts = incidentRepository.countIncidentsGroupedBySeverity();
        for (Object[] row : incidentCounts) {
            if (row[0] != null) {
                incidentsBySev.put(row[0].toString(), (Long) row[1]);
            }
        }

        // 4. Calculate Security Posture Score
        int postureScore = calculateSecurityPosture(incidentsBySev);

        // 5. Build DTO
        DashboardMetricsDto metrics = DashboardMetricsDto.builder()
                .totalLogs(totalLogs)
                .activeIncidents(activeIncidents)
                .criticalAlerts(criticalAlerts)
                .securityPostureScore(postureScore)
                .logsByEventType(logsByType)
                .alertsBySeverity(alertsBySev)
                .incidentsBySeverity(incidentsBySev)
                .riskScores(Map.of(
                        "userRisk", 15,    // general baseline
                        "assetRisk", 30,   // general baseline
                        "incidentRisk", 45  // general baseline
                ))
                .build();

        return ResponseEntity.ok(metrics);
    }

    private int calculateSecurityPosture(Map<String, Long> incidentsBySev) {
        int base = 100;

        long criticalCount = incidentsBySev.getOrDefault("CRITICAL", 0L);
        long highCount = incidentsBySev.getOrDefault("HIGH", 0L);
        long mediumCount = incidentsBySev.getOrDefault("MEDIUM", 0L);
        long lowCount = incidentsBySev.getOrDefault("LOW", 0L);

        // Deduct based on active incidents
        int deductions = (int) (criticalCount * 12 + highCount * 7 + mediumCount * 4 + lowCount * 1);
        int finalScore = base - deductions;

        return Math.max(10, Math.min(100, finalScore)); // Posture score bounds: 10 - 100
    }
}

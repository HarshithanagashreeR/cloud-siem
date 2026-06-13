package com.amazon.siem.service.alert;

import com.amazon.siem.model.Alert;
import com.amazon.siem.model.Incident;
import com.amazon.siem.model.User;
import com.amazon.siem.repository.AlertRepository;
import com.amazon.siem.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class IncidentServiceImpl implements IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Override
    @Transactional
    public Incident createIncident(String title, String description, String severity, List<UUID> alertIds) {
        Set<Alert> alerts = new HashSet<>();
        if (alertIds != null && !alertIds.isEmpty()) {
            alerts.addAll(alertRepository.findAllById(alertIds));
        }

        int riskScore = calculateIncidentRiskScore(alerts);

        Incident incident = Incident.builder()
                .title(title)
                .description(description)
                .severity(severity.toUpperCase())
                .status("ACTIVE")
                .riskScore(riskScore)
                .alerts(alerts)
                .build();

        return incidentRepository.save(incident);
    }

    @Override
    @Transactional
    public Incident assignAnalyst(UUID incidentId, User analyst) {
        Incident incident = getIncidentById(incidentId);
        incident.setAnalyst(analyst);
        incident.setStatus("INVESTIGATING");
        return incidentRepository.save(incident);
    }

    @Override
    @Transactional
    public Incident updateStatus(UUID incidentId, String status) {
        Incident incident = getIncidentById(incidentId);
        incident.setStatus(status.toUpperCase());
        if ("RESOLVED".equalsIgnoreCase(status)) {
            incident.setClosedAt(LocalDateTime.now());
            // Resolve associated alerts too
            incident.getAlerts().forEach(alert -> {
                alert.setStatus("RESOLVED");
                alert.setResolvedAt(LocalDateTime.now());
                alertRepository.save(alert);
            });
        }
        return incidentRepository.save(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Incident getIncidentById(UUID incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + incidentId));
    }

    private int calculateIncidentRiskScore(Set<Alert> alerts) {
        if (alerts == null || alerts.isEmpty()) {
            return 10; // baseline risk
        }

        int maxSeverityWeight = 0;
        for (Alert alert : alerts) {
            int weight = switch (alert.getSeverity().toUpperCase()) {
                case "CRITICAL" -> 90;
                case "HIGH" -> 70;
                case "MEDIUM" -> 40;
                case "LOW" -> 20;
                default -> 10;
            };
            if (weight > maxSeverityWeight) {
                maxSeverityWeight = weight;
            }
        }

        // Add modifiers based on count of related alerts (+5 per alert up to maximum 100)
        int countModifier = (alerts.size() - 1) * 5;
        int finalScore = maxSeverityWeight + countModifier;
        return Math.min(100, Math.max(0, finalScore));
    }
}

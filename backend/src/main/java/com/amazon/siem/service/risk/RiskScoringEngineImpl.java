package com.amazon.siem.service.risk;

import com.amazon.siem.model.Alert;
import com.amazon.siem.model.Incident;
import com.amazon.siem.model.ThreatIntel;
import com.amazon.siem.repository.AlertRepository;
import com.amazon.siem.repository.IncidentRepository;
import com.amazon.siem.repository.ThreatIntelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class RiskScoringEngineImpl implements RiskScoringEngine {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ThreatIntelRepository threatIntelRepository;

    @Override
    @Transactional(readOnly = true)
    public int calculateUserRisk(UUID userId) {
        // Compute user risk based on incidents assigned or targeted.
        // For simplicity: aggregate active incidents assigned to the user
        List<Incident> incidents = incidentRepository.findAll();
        long activeCount = incidents.stream()
                .filter(i -> i.getAnalyst() != null && i.getAnalyst().getId().equals(userId) && !"RESOLVED".equals(i.getStatus()))
                .count();

        int score = (int) (activeCount * 15 + 10); // baseline 10, +15 per active assigned incident
        return Math.min(100, Math.max(10, score));
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateAssetRisk(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return 0;
        }

        // Aggregate alerts targeting this source IP
        List<Alert> alerts = alertRepository.findAll().stream()
                .filter(a -> ipAddress.equals(a.getSourceIp()) && !"RESOLVED".equals(a.getStatus()))
                .toList();

        int score = 0;
        for (Alert alert : alerts) {
            score += switch (alert.getSeverity().toUpperCase()) {
                case "CRITICAL" -> 35;
                case "HIGH" -> 25;
                case "MEDIUM" -> 15;
                case "LOW" -> 5;
                default -> 2;
            };
        }

        // Add modifier if the IP is blacklisted in Threat Intelligence
        if (threatIntelRepository.existsByIndicator(ipAddress)) {
            ThreatIntel intel = threatIntelRepository.findByIndicator(ipAddress).orElse(null);
            if (intel != null) {
                score += (int) (intel.getRiskScore() * 0.4); // add 40% of its threat intelligence score
            } else {
                score += 20;
            }
        }

        return Math.min(100, Math.max(0, score));
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateIncidentRisk(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (incident == null) {
            return 0;
        }
        return incident.getRiskScore();
    }
}

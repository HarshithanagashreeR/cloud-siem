package com.amazon.siem.service.alert;

import com.amazon.siem.model.Alert;
import com.amazon.siem.model.Incident;
import com.amazon.siem.model.LogEntry;
import com.amazon.siem.model.ThreatIntel;
import com.amazon.siem.repository.LogRepository;
import com.amazon.siem.repository.ThreatIntelRepository;
import com.amazon.siem.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ThreatDetectionEngineImpl implements ThreatDetectionEngine {
    private static final Logger logger = LoggerFactory.getLogger(ThreatDetectionEngineImpl.class);

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ThreatIntelRepository threatIntelRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AlertService alertService;

    @Autowired
    private IncidentService incidentService;

    @Override
    @Transactional
    public void analyzeLog(LogEntry log) {
        String sourceIp = log.getSourceIp();
        LocalDateTime now = LocalDateTime.now();

        // Rule 1: Suspicious IP traffic (Threat Intelligence Match)
        Optional<ThreatIntel> intelOpt = threatIntelRepository.findByIndicator(sourceIp);
        if (intelOpt.isPresent()) {
            ThreatIntel intel = intelOpt.get();
            String description = String.format("Traffic detected from indicator %s linked to threat %s (%s). Source Description: %s",
                    sourceIp, intel.getSource(), intel.getType(), intel.getDescription());
            createAndCorrelateAlert(
                    "Threat Intel Match: " + sourceIp,
                    description,
                    intel.getRiskScore() > 70 ? "CRITICAL" : "HIGH",
                    "THREAT_INTEL",
                    sourceIp
            );
        }

        // Rule 2: Brute Force (Multiple Failed Logins)
        if ("LOGIN_FAILED".equalsIgnoreCase(log.getEventType())) {
            List<LogEntry> failedLogins = logRepository.findBySourceIpAndTimestampAfter(sourceIp, now.minusMinutes(5))
                    .stream()
                    .filter(l -> "LOGIN_FAILED".equalsIgnoreCase(l.getEventType()))
                    .toList();

            if (failedLogins.size() >= 5) {
                createAndCorrelateAlert(
                        "Brute Force Login Attack",
                        String.format("Detected %d failed authentication attempts from IP %s in the last 5 minutes.", failedLogins.size(), sourceIp),
                        "HIGH",
                        "BRUTE_FORCE",
                        sourceIp
                );
            }
        }

        // Rule 3: Port Scanning
        if (log.getDestinationPort() != null) {
            List<Integer> ports = logRepository.findDistinctDestinationPortsBySourceIpAfter(sourceIp, now.minusMinutes(1));
            if (ports.size() >= 10) {
                createAndCorrelateAlert(
                        "Reconnaissance: Port Scan Detected",
                        String.format("Source IP %s connected to %d distinct destination ports within 1 minute.", sourceIp, ports.size()),
                        "CRITICAL",
                        "PORT_SCAN",
                        sourceIp
                );
            }
        }

        // Rule 4: Excessive Request Rate (Potential DoS)
        List<LogEntry> recentLogs = logRepository.findBySourceIpAndTimestampAfter(sourceIp, now.minusSeconds(10));
        if (recentLogs.size() >= 100) {
            createAndCorrelateAlert(
                    "Excessive Request Rate / DoS Signature",
                    String.format("IP %s triggered %d requests within 10 seconds. Exceeds standard service threshold.", sourceIp, recentLogs.size()),
                    "MEDIUM",
                    "RATE_LIMIT_EXCEEDED",
                    sourceIp
            );
        }

        // Rule 5: Privilege Escalation
        if ("PRIVILEGE_ESCALATION".equalsIgnoreCase(log.getEventType()) || 
            (log.getMessage() != null && log.getMessage().toLowerCase().contains("privilege escalation"))) {
            createAndCorrelateAlert(
                    "Privilege Escalation Event",
                    String.format("Security log triggers escalation signature from source IP %s: %s", sourceIp, log.getMessage()),
                    "CRITICAL",
                    "PRIVILEGE_ESCALATION",
                    sourceIp
            );
        }
    }

    private void createAndCorrelateAlert(String title, String description, String severity, String threatType, String sourceIp) {
        logger.info("Threat Engine Triggered: {} - Source IP: {}", title, sourceIp);
        
        // 1. Create the alert
        Alert alert = alertService.createAlert(title, description, severity, threatType, sourceIp);

        // 2. Correlate to an Incident
        // Look for any ACTIVE/INVESTIGATING Incident related to this source IP created in the last 15 minutes
        List<Incident> activeIncidents = incidentRepository.findByStatus("ACTIVE");
        activeIncidents.addAll(incidentRepository.findByStatus("INVESTIGATING"));

        Incident correlatedIncident = null;
        for (Incident inc : activeIncidents) {
            boolean matchesIp = inc.getAlerts().stream()
                    .anyMatch(a -> sourceIp.equals(a.getSourceIp()) && a.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(15)));
            if (matchesIp) {
                correlatedIncident = inc;
                break;
            }
        }

        if (correlatedIncident != null) {
            // Add alert to existing incident
            correlatedIncident.getAlerts().add(alert);
            // Re-evaluate incident severity and risk score
            int updatedRisk = calculateUpdatedRisk(correlatedIncident.getAlerts());
            correlatedIncident.setRiskScore(updatedRisk);
            if ("CRITICAL".equalsIgnoreCase(severity)) {
                correlatedIncident.setSeverity("CRITICAL");
            }
            incidentRepository.save(correlatedIncident);
            logger.info("Correlated alert {} to existing incident: {}", alert.getId(), correlatedIncident.getTitle());
        } else {
            // Create a new incident
            String incTitle = "Security Incident: " + threatType + " targeting IP " + sourceIp;
            String incDesc = "Correlated threats detected. Primary alert: " + title;
            incidentService.createIncident(incTitle, incDesc, severity, List.of(alert.getId()));
            logger.info("Created new incident for correlated alert: {}", alert.getId());
        }
    }

    private int calculateUpdatedRisk(java.util.Set<Alert> alerts) {
        int maxSeverityWeight = 0;
        for (Alert a : alerts) {
            int weight = switch (a.getSeverity().toUpperCase()) {
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
        int countModifier = (alerts.size() - 1) * 5;
        return Math.min(100, maxSeverityWeight + countModifier);
    }
}

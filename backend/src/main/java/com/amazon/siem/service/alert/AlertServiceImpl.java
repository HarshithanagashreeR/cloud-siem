package com.amazon.siem.service.alert;

import com.amazon.siem.model.Alert;
import com.amazon.siem.model.User;
import com.amazon.siem.repository.AlertRepository;
import com.amazon.siem.service.aws.AwsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AwsService awsService;

    @Override
    @Transactional
    public Alert createAlert(String title, String description, String severity, String threatType, String sourceIp) {
        Alert alert = Alert.builder()
                .title(title)
                .description(description)
                .severity(severity.toUpperCase())
                .threatType(threatType)
                .sourceIp(sourceIp)
                .status("OPEN")
                .build();

        Alert savedAlert = alertRepository.save(alert);

        // AWS Integration - Trigger SNS notification for critical alerts
        if ("CRITICAL".equalsIgnoreCase(severity)) {
            String subject = "SIEM CRITICAL ALERT: " + title;
            String message = String.format(
                    "Severity: CRITICAL\nThreat Type: %s\nSource IP: %s\nTime: %s\n\nDescription:\n%s",
                    threatType, sourceIp, LocalDateTime.now(), description
            );
            awsService.publishCriticalAlert(subject, message);
        }

        return savedAlert;
    }

    @Override
    @Transactional
    public Alert acknowledgeAlert(UUID alertId, User analyst) {
        Alert alert = getAlertById(alertId);
        alert.setStatus("ACKNOWLEDGED");
        alert.setAnalyst(analyst);
        return alertRepository.save(alert);
    }

    @Override
    @Transactional
    public Alert resolveAlert(UUID alertId) {
        Alert alert = getAlertById(alertId);
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Alert getAlertById(UUID alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));
    }
}

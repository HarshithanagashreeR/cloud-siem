package com.amazon.siem.service.risk;

import java.util.UUID;

public interface RiskScoringEngine {
    int calculateUserRisk(UUID userId);
    int calculateAssetRisk(String ipAddress);
    int calculateIncidentRisk(UUID incidentId);
}

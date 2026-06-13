package com.amazon.siem.dto;

import java.util.Map;

public class DashboardMetricsDto {
    private long totalLogs;
    private long activeIncidents;
    private long criticalAlerts;
    private int securityPostureScore; // 0 to 100
    private Map<String, Long> logsByEventType;
    private Map<String, Long> alertsBySeverity;
    private Map<String, Long> incidentsBySeverity;
    private Map<String, Integer> riskScores;

    public DashboardMetricsDto() {}

    public DashboardMetricsDto(long totalLogs, long activeIncidents, long criticalAlerts, int securityPostureScore, 
                               Map<String, Long> logsByEventType, Map<String, Long> alertsBySeverity, 
                               Map<String, Long> incidentsBySeverity, Map<String, Integer> riskScores) {
        this.totalLogs = totalLogs;
        this.activeIncidents = activeIncidents;
        this.criticalAlerts = criticalAlerts;
        this.securityPostureScore = securityPostureScore;
        this.logsByEventType = logsByEventType;
        this.alertsBySeverity = alertsBySeverity;
        this.incidentsBySeverity = incidentsBySeverity;
        this.riskScores = riskScores;
    }

    public long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public long getActiveIncidents() {
        return activeIncidents;
    }

    public void setActiveIncidents(long activeIncidents) {
        this.activeIncidents = activeIncidents;
    }

    public long getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(long criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public int getSecurityPostureScore() {
        return securityPostureScore;
    }

    public void setSecurityPostureScore(int securityPostureScore) {
        this.securityPostureScore = securityPostureScore;
    }

    public Map<String, Long> getLogsByEventType() {
        return logsByEventType;
    }

    public void setLogsByEventType(Map<String, Long> logsByEventType) {
        this.logsByEventType = logsByEventType;
    }

    public Map<String, Long> getAlertsBySeverity() {
        return alertsBySeverity;
    }

    public void setAlertsBySeverity(Map<String, Long> alertsBySeverity) {
        this.alertsBySeverity = alertsBySeverity;
    }

    public Map<String, Long> getIncidentsBySeverity() {
        return incidentsBySeverity;
    }

    public void setIncidentsBySeverity(Map<String, Long> incidentsBySeverity) {
        this.incidentsBySeverity = incidentsBySeverity;
    }

    public Map<String, Integer> getRiskScores() {
        return riskScores;
    }

    public void setRiskScores(Map<String, Integer> riskScores) {
        this.riskScores = riskScores;
    }

    public static DashboardMetricsDtoBuilder builder() {
        return new DashboardMetricsDtoBuilder();
    }

    public static class DashboardMetricsDtoBuilder {
        private long totalLogs;
        private long activeIncidents;
        private long criticalAlerts;
        private int securityPostureScore;
        private Map<String, Long> logsByEventType;
        private Map<String, Long> alertsBySeverity;
        private Map<String, Long> incidentsBySeverity;
        private Map<String, Integer> riskScores;

        public DashboardMetricsDtoBuilder totalLogs(long totalLogs) {
            this.totalLogs = totalLogs;
            return this;
        }

        public DashboardMetricsDtoBuilder activeIncidents(long activeIncidents) {
            this.activeIncidents = activeIncidents;
            return this;
        }

        public DashboardMetricsDtoBuilder criticalAlerts(long criticalAlerts) {
            this.criticalAlerts = criticalAlerts;
            return this;
        }

        public DashboardMetricsDtoBuilder securityPostureScore(int securityPostureScore) {
            this.securityPostureScore = securityPostureScore;
            return this;
        }

        public DashboardMetricsDtoBuilder logsByEventType(Map<String, Long> logsByEventType) {
            this.logsByEventType = logsByEventType;
            return this;
        }

        public DashboardMetricsDtoBuilder alertsBySeverity(Map<String, Long> alertsBySeverity) {
            this.alertsBySeverity = alertsBySeverity;
            return this;
        }

        public DashboardMetricsDtoBuilder incidentsBySeverity(Map<String, Long> incidentsBySeverity) {
            this.incidentsBySeverity = incidentsBySeverity;
            return this;
        }

        public DashboardMetricsDtoBuilder riskScores(Map<String, Integer> riskScores) {
            this.riskScores = riskScores;
            return this;
        }

        public DashboardMetricsDto build() {
            return new DashboardMetricsDto(totalLogs, activeIncidents, criticalAlerts, securityPostureScore, logsByEventType, alertsBySeverity, incidentsBySeverity, riskScores);
        }
    }
}

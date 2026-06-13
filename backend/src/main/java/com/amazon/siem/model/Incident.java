package com.amazon.siem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 150, nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 20, nullable = false)
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW

    @Column(length = 20, nullable = false)
    private String status; // ACTIVE, INVESTIGATING, RESOLVED

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "analyst_id")
    private User analyst;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "incident_alerts",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "alert_id")
    )
    private Set<Alert> alerts = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public Incident() {}

    public Incident(UUID id, String title, String description, String severity, String status, Integer riskScore, 
                    User analyst, Set<Alert> alerts, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime closedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.riskScore = riskScore != null ? riskScore : 0;
        this.analyst = analyst;
        this.alerts = alerts != null ? alerts : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
        if (riskScore == null) {
            riskScore = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public User getAnalyst() {
        return analyst;
    }

    public void setAnalyst(User analyst) {
        this.analyst = analyst;
    }

    public Set<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public static IncidentBuilder builder() {
        return new IncidentBuilder();
    }

    public static class IncidentBuilder {
        private UUID id;
        private String title;
        private String description;
        private String severity;
        private String status;
        private Integer riskScore;
        private User analyst;
        private Set<Alert> alerts;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime closedAt;

        public IncidentBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public IncidentBuilder title(String title) {
            this.title = title;
            return this;
        }

        public IncidentBuilder description(String description) {
            this.description = description;
            return this;
        }

        public IncidentBuilder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public IncidentBuilder status(String status) {
            this.status = status;
            return this;
        }

        public IncidentBuilder riskScore(Integer riskScore) {
            this.riskScore = riskScore;
            return this;
        }

        public IncidentBuilder analyst(User analyst) {
            this.analyst = analyst;
            return this;
        }

        public IncidentBuilder alerts(Set<Alert> alerts) {
            this.alerts = alerts;
            return this;
        }

        public IncidentBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public IncidentBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public IncidentBuilder closedAt(LocalDateTime closedAt) {
            this.closedAt = closedAt;
            return this;
        }

        public Incident build() {
            return new Incident(id, title, description, severity, status, riskScore, analyst, alerts, createdAt, updatedAt, closedAt);
        }
    }
}

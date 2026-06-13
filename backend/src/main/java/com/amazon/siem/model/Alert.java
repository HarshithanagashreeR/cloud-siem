package com.amazon.siem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class Alert {

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
    private String status; // OPEN, ACKNOWLEDGED, RESOLVED

    @Column(name = "threat_type", length = 50, nullable = false)
    private String threatType;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User analyst;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public Alert() {}

    public Alert(UUID id, String title, String description, String severity, String status, String threatType, 
                 String sourceIp, User analyst, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.threatType = threatType;
        this.sourceIp = sourceIp;
        this.analyst = analyst;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "OPEN";
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

    public String getThreatType() {
        return threatType;
    }

    public void setThreatType(String threatType) {
        this.threatType = threatType;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public User getAnalyst() {
        return analyst;
    }

    public void setAnalyst(User analyst) {
        this.analyst = analyst;
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

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public static AlertBuilder builder() {
        return new AlertBuilder();
    }

    public static class AlertBuilder {
        private UUID id;
        private String title;
        private String description;
        private String severity;
        private String status;
        private String threatType;
        private String sourceIp;
        private User analyst;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;

        public AlertBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public AlertBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AlertBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AlertBuilder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public AlertBuilder status(String status) {
            this.status = status;
            return this;
        }

        public AlertBuilder threatType(String threatType) {
            this.threatType = threatType;
            return this;
        }

        public AlertBuilder sourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
            return this;
        }

        public AlertBuilder analyst(User analyst) {
            this.analyst = analyst;
            return this;
        }

        public AlertBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AlertBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AlertBuilder resolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public Alert build() {
            return new Alert(id, title, description, severity, status, threatType, sourceIp, analyst, createdAt, updatedAt, resolvedAt);
        }
    }
}

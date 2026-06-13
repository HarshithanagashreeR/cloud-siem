package com.amazon.siem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "threat_intelligence")
public class ThreatIntel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 100, unique = true, nullable = false)
    private String indicator;

    @Column(length = 50, nullable = false)
    private String type; // IP, DOMAIN, FILE_HASH

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(length = 100, nullable = false)
    private String source;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ThreatIntel() {}

    public ThreatIntel(UUID id, String indicator, String type, Integer riskScore, String source, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.indicator = indicator;
        this.type = type;
        this.riskScore = riskScore;
        this.source = source;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public static ThreatIntelBuilder builder() {
        return new ThreatIntelBuilder();
    }

    public static class ThreatIntelBuilder {
        private UUID id;
        private String indicator;
        private String type;
        private Integer riskScore;
        private String source;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ThreatIntelBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ThreatIntelBuilder indicator(String indicator) {
            this.indicator = indicator;
            return this;
        }

        public ThreatIntelBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ThreatIntelBuilder riskScore(Integer riskScore) {
            this.riskScore = riskScore;
            return this;
        }

        public ThreatIntelBuilder source(String source) {
            this.source = source;
            return this;
        }

        public ThreatIntelBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ThreatIntelBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ThreatIntelBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ThreatIntel build() {
            return new ThreatIntel(id, indicator, type, riskScore, source, description, createdAt, updatedAt);
        }
    }
}

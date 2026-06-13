package com.amazon.siem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String type; // PDF_SECURITY, INCIDENT_REPORT, EXEC_SUMMARY

    @Column(name = "s3_url", length = 255, nullable = false)
    private String s3Url;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Report() {}

    public Report(UUID id, String name, String type, String s3Url, User createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.s3Url = s3Url;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static ReportBuilder builder() {
        return new ReportBuilder();
    }

    public static class ReportBuilder {
        private UUID id;
        private String name;
        private String type;
        private String s3Url;
        private User createdBy;
        private LocalDateTime createdAt;

        public ReportBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ReportBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ReportBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ReportBuilder s3Url(String s3Url) {
            this.s3Url = s3Url;
            return this;
        }

        public ReportBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public ReportBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Report build() {
            return new Report(id, name, type, s3Url, createdBy, createdAt);
        }
    }
}

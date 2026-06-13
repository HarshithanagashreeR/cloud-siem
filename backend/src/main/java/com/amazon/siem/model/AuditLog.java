package com.amazon.siem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 100, nullable = false)
    private String actor;

    @Column(length = 100, nullable = false)
    private String action;

    @Column(length = 100, nullable = false)
    private String resource;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(columnDefinition = "text")
    private String details;

    public AuditLog() {}

    public AuditLog(UUID id, LocalDateTime timestamp, String actor, String action, String resource, String status, String ipAddress, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.actor = actor;
        this.action = action;
        this.resource = resource;
        this.status = status;
        this.ipAddress = ipAddress;
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private UUID id;
        private LocalDateTime timestamp;
        private String actor;
        private String action;
        private String resource;
        private String status;
        private String ipAddress;
        private String details;

        public AuditLogBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public AuditLogBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditLogBuilder actor(String actor) {
            this.actor = actor;
            return this;
        }

        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public AuditLogBuilder status(String status) {
            this.status = status;
            return this;
        }

        public AuditLogBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public AuditLogBuilder details(String details) {
            this.details = details;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(id, timestamp, actor, action, resource, status, ipAddress, details);
        }
    }
}

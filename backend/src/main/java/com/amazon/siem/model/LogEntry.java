package com.amazon.siem.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "logs")
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "source_ip", length = 45, nullable = false)
    private String sourceIp;

    @Column(name = "destination_ip", length = 45)
    private String destinationIp;

    @Column(name = "destination_port")
    private Integer destinationPort;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(length = 20, nullable = false)
    private String severity;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(columnDefinition = "text")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LogEntry() {}

    public LogEntry(UUID id, LocalDateTime timestamp, String sourceIp, String destinationIp, Integer destinationPort, 
                    String eventType, JsonNode payload, String severity, String status, String message, LocalDateTime createdAt) {
        this.id = id;
        this.timestamp = timestamp;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.eventType = eventType;
        this.payload = payload;
        this.severity = severity;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = "UNPROCESSED";
        }
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

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public Integer getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Integer destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static LogEntryBuilder builder() {
        return new LogEntryBuilder();
    }

    public static class LogEntryBuilder {
        private UUID id;
        private LocalDateTime timestamp;
        private String sourceIp;
        private String destinationIp;
        private Integer destinationPort;
        private String eventType;
        private JsonNode payload;
        private String severity;
        private String status;
        private String message;
        private LocalDateTime createdAt;

        public LogEntryBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public LogEntryBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public LogEntryBuilder sourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
            return this;
        }

        public LogEntryBuilder destinationIp(String destinationIp) {
            this.destinationIp = destinationIp;
            return this;
        }

        public LogEntryBuilder destinationPort(Integer destinationPort) {
            this.destinationPort = destinationPort;
            return this;
        }

        public LogEntryBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public LogEntryBuilder payload(JsonNode payload) {
            this.payload = payload;
            return this;
        }

        public LogEntryBuilder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public LogEntryBuilder status(String status) {
            this.status = status;
            return this;
        }

        public LogEntryBuilder message(String message) {
            this.message = message;
            return this;
        }

        public LogEntryBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LogEntry build() {
            return new LogEntry(id, timestamp, sourceIp, destinationIp, destinationPort, eventType, payload, severity, status, message, createdAt);
        }
    }
}

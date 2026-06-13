package com.amazon.siem.service.ingestion;

import com.amazon.siem.dto.LogDto;
import com.amazon.siem.model.LogEntry;
import com.amazon.siem.repository.LogRepository;
import com.amazon.siem.service.alert.ThreatDetectionEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class LogIngestionServiceImpl implements LogIngestionService {
    private static final Logger logger = LoggerFactory.getLogger(LogIngestionServiceImpl.class);

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ThreatDetectionEngine threatDetectionEngine;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Utilize Java 21 Virtual Threads for non-blocking concurrent log analysis
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    @Transactional
    public LogEntry ingestSingleLog(LogDto dto) {
        LogEntry logEntry = mapDtoToEntity(dto);
        LogEntry saved = logRepository.save(logEntry);

        // Async analysis via Virtual Threads
        virtualThreadExecutor.submit(() -> {
            try {
                threatDetectionEngine.analyzeLog(saved);
            } catch (Exception e) {
                logger.error("Error analyzing log in virtual thread: {}", e.getMessage(), e);
            }
        });

        return saved;
    }

    @Override
    @Transactional
    public List<LogEntry> ingestLogs(List<LogDto> logDtos) {
        if (logDtos == null || logDtos.isEmpty()) {
            return List.of();
        }

        logger.info("Starting bulk ingestion of {} logs", logDtos.size());
        List<LogEntry> logsToSave = logDtos.stream().map(this::mapDtoToEntity).toList();

        // High performance database write using batch JDBC
        String sql = "INSERT INTO logs (id, timestamp, source_ip, destination_ip, destination_port, event_type, payload, severity, status, message, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                LogEntry log = logsToSave.get(i);
                ps.setObject(1, log.getId());
                ps.setTimestamp(2, Timestamp.valueOf(log.getTimestamp()));
                ps.setString(3, log.getSourceIp());
                ps.setString(4, log.getDestinationIp());
                if (log.getDestinationPort() != null) {
                    ps.setInt(5, log.getDestinationPort());
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
                ps.setString(6, log.getEventType());
                
                // Convert Jackson JsonNode to String representing jsonb DDL
                String jsonStr = "{}";
                if (log.getPayload() != null) {
                    try {
                        jsonStr = objectMapper.writeValueAsString(log.getPayload());
                    } catch (Exception ignored) {}
                }
                ps.setString(7, jsonStr);
                ps.setString(8, log.getSeverity());
                ps.setString(9, log.getStatus());
                ps.setString(10, log.getMessage());
                ps.setTimestamp(11, Timestamp.valueOf(log.getCreatedAt()));
            }

            @Override
            public int getBatchSize() {
                return logsToSave.size();
            }
        });

        logger.info("Successfully ingested {} logs via JDBC batch insert.", logsToSave.size());

        // Asynchronously analyze logs in parallel using virtual threads
        for (LogEntry log : logsToSave) {
            virtualThreadExecutor.submit(() -> {
                try {
                    threatDetectionEngine.analyzeLog(log);
                } catch (Exception e) {
                    logger.error("Error analyzing bulk log {} in virtual thread: {}", log.getId(), e.getMessage());
                }
            });
        }

        return logsToSave;
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LogEntry> getAllLogs(org.springframework.data.domain.Pageable pageable) {
        return logRepository.findAll(pageable);
    }

    private LogEntry mapDtoToEntity(LogDto dto) {
        LocalDateTime time = dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now();
        return LogEntry.builder()
                .id(UUID.randomUUID())
                .timestamp(time)
                .sourceIp(dto.getSourceIp())
                .destinationIp(dto.getDestinationIp())
                .destinationPort(dto.getDestinationPort())
                .eventType(dto.getEventType())
                .payload(dto.getPayload())
                .severity(dto.getSeverity())
                .status("UNPROCESSED")
                .message(dto.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
    }
}

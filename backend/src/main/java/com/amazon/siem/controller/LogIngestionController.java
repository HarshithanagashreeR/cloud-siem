package com.amazon.siem.controller;

import com.amazon.siem.dto.LogDto;
import com.amazon.siem.dto.MessageResponse;
import com.amazon.siem.model.LogEntry;
import com.amazon.siem.service.ingestion.LogIngestionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
public class LogIngestionController {
    private static final Logger logger = LoggerFactory.getLogger(LogIngestionController.class);

    @Autowired
    private LogIngestionService logIngestionService;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestSingleLog(@Valid @RequestBody LogDto logDto) {
        try {
            LogEntry saved = logIngestionService.ingestSingleLog(logDto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Failed to ingest single log: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Ingestion failed: " + e.getMessage()));
        }
    }

    @PostMapping("/ingest/bulk")
    public ResponseEntity<?> ingestBulkLogs(@Valid @RequestBody List<LogDto> logDtos) {
        try {
            List<LogEntry> saved = logIngestionService.ingestLogs(logDtos);
            return ResponseEntity.ok(new MessageResponse(String.format("Ingested %d logs successfully.", saved.size())));
        } catch (Exception e) {
            logger.error("Failed to ingest bulk logs: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Bulk Ingestion failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLogs(org.springframework.data.domain.Pageable pageable) {
        try {
            return ResponseEntity.ok(logIngestionService.getAllLogs(pageable));
        } catch (Exception e) {
            logger.error("Failed to fetch logs: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to fetch logs: " + e.getMessage()));
        }
    }
}

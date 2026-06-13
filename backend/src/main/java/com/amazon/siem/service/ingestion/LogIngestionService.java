package com.amazon.siem.service.ingestion;

import com.amazon.siem.dto.LogDto;
import com.amazon.siem.model.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface LogIngestionService {
    LogEntry ingestSingleLog(LogDto logDto);
    List<LogEntry> ingestLogs(List<LogDto> logDtos);
    Page<LogEntry> getAllLogs(Pageable pageable);
}

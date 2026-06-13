package com.amazon.siem.repository;

import com.amazon.siem.model.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LogRepository extends JpaRepository<LogEntry, UUID> {
    Page<LogEntry> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT COUNT(l) FROM LogEntry l")
    long countAllLogs();

    @Query("SELECT l.eventType, COUNT(l) FROM LogEntry l GROUP BY l.eventType")
    List<Object[]> countLogsGroupedByEventType();

    List<LogEntry> findBySourceIpAndTimestampAfter(String sourceIp, LocalDateTime timestamp);

    @Query("SELECT DISTINCT l.destinationPort FROM LogEntry l WHERE l.sourceIp = ?1 AND l.timestamp > ?2")
    List<Integer> findDistinctDestinationPortsBySourceIpAfter(String sourceIp, LocalDateTime timestamp);
}

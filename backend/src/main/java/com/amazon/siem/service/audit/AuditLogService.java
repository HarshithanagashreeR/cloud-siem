package com.amazon.siem.service.audit;

import com.amazon.siem.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void logAction(String actor, String action, String resource, String status, String ipAddress, String details);
    Page<AuditLog> getAuditLogs(Pageable pageable);
}

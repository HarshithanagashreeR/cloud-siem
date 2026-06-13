package com.amazon.siem.service.audit;

import com.amazon.siem.model.AuditLog;
import com.amazon.siem.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logAction(String actor, String action, String resource, String status, String ipAddress, String details) {
        AuditLog audit = AuditLog.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .status(status)
                .ipAddress(ipAddress)
                .details(details)
                .build();
        auditLogRepository.save(audit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }
}

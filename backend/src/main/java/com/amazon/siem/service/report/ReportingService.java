package com.amazon.siem.service.report;

import com.amazon.siem.model.Report;
import com.amazon.siem.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReportingService {
    Report generateIncidentReportPdf(UUID incidentId, User creator);
    Report generateSystemSecurityReportPdf(LocalDateTime start, LocalDateTime end, User creator);
    List<Report> getAllReports();
}

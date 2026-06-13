package com.amazon.siem.service.ai;

import com.amazon.siem.dto.AiReportResponseDto;
import java.util.UUID;

public interface AiSecurityAnalystService {
    AiReportResponseDto analyzeIncident(UUID incidentId);
}

package com.amazon.siem.service.alert;

import com.amazon.siem.model.LogEntry;

public interface ThreatDetectionEngine {
    void analyzeLog(LogEntry log);
}

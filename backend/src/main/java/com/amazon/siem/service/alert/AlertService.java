package com.amazon.siem.service.alert;

import com.amazon.siem.model.Alert;
import com.amazon.siem.model.User;
import java.util.List;
import java.util.UUID;

public interface AlertService {
    Alert createAlert(String title, String description, String severity, String threatType, String sourceIp);
    Alert acknowledgeAlert(UUID alertId, User analyst);
    Alert resolveAlert(UUID alertId);
    List<Alert> getAllAlerts();
    Alert getAlertById(UUID alertId);
}

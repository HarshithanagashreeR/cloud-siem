package com.amazon.siem.service.alert;

import com.amazon.siem.model.Incident;
import com.amazon.siem.model.User;
import java.util.List;
import java.util.UUID;

public interface IncidentService {
    Incident createIncident(String title, String description, String severity, List<UUID> alertIds);
    Incident assignAnalyst(UUID incidentId, User analyst);
    Incident updateStatus(UUID incidentId, String status);
    List<Incident> getAllIncidents();
    Incident getIncidentById(UUID incidentId);
}

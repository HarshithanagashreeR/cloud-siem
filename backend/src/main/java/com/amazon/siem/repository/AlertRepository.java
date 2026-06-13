package com.amazon.siem.repository;

import com.amazon.siem.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByStatus(String status);
    List<Alert> findBySeverity(String severity);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status = 'OPEN'")
    long countActiveAlerts();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.severity = 'CRITICAL' AND a.status = 'OPEN'")
    long countCriticalActiveAlerts();

    @Query("SELECT a.severity, COUNT(a) FROM Alert a GROUP BY a.severity")
    List<Object[]> countAlertsGroupedBySeverity();
}

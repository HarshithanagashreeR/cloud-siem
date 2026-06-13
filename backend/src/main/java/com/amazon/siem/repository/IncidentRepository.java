package com.amazon.siem.repository;

import com.amazon.siem.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findByStatus(String status);
    List<Incident> findBySeverity(String severity);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status <> 'RESOLVED'")
    long countActiveIncidents();

    @Query("SELECT i.severity, COUNT(i) FROM Incident i GROUP BY i.severity")
    List<Object[]> countIncidentsGroupedBySeverity();
}

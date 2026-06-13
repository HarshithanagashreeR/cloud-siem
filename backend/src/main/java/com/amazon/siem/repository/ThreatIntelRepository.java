package com.amazon.siem.repository;

import com.amazon.siem.model.ThreatIntel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThreatIntelRepository extends JpaRepository<ThreatIntel, UUID> {
    Optional<ThreatIntel> findByIndicator(String indicator);
    boolean existsByIndicator(String indicator);
}

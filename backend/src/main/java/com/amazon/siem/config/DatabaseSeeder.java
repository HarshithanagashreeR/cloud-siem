package com.amazon.siem.config;

import com.amazon.siem.model.ThreatIntel;
import com.amazon.siem.repository.ThreatIntelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private ThreatIntelRepository threatIntelRepository;

    @Override
    public void run(String... args) throws Exception {
        seedThreatIntel();
    }

    private void seedThreatIntel() {
        if (threatIntelRepository.count() == 0) {
            // Seed a few malicious IP indicators
            threatIntelRepository.save(ThreatIntel.builder()
                    .indicator("185.220.101.4")
                    .type("IP")
                    .riskScore(95)
                    .source("Tor Project Exit List")
                    .description("Known Tor Exit Node actively associated with malicious credential stuffing.")
                    .build());

            threatIntelRepository.save(ThreatIntel.builder()
                    .indicator("185.220.101.5")
                    .type("IP")
                    .riskScore(80)
                    .source("Spamhaus DROP")
                    .description("Associated with botnet command and control traffic.")
                    .build());

            threatIntelRepository.save(ThreatIntel.builder()
                    .indicator("8.8.8.8")
                    .type("IP")
                    .riskScore(10)
                    .source("Google Public DNS")
                    .description("Benign indicator, used for baseline test.")
                    .build());
        }
    }
}

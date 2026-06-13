package com.amazon.siem.service.ai;

import com.amazon.siem.config.AiConfig;
import com.amazon.siem.dto.AiReportResponseDto;
import com.amazon.siem.model.Alert;
import com.amazon.siem.model.Incident;
import com.amazon.siem.repository.IncidentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AiSecurityAnalystServiceImpl implements AiSecurityAnalystService {
    private static final Logger logger = LoggerFactory.getLogger(AiSecurityAnalystServiceImpl.class);

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    @Transactional(readOnly = true)
    public AiReportResponseDto analyzeIncident(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + incidentId));

        String prompt = buildPrompt(incident);

        // Check if API key is not configured or is a mock/placeholder
        String apiKey = aiConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank() || "mock_ai_key".equals(apiKey) || !apiKey.startsWith("AIzaSy")) {
            logger.info("Using mock AI security analyst (no valid API key starting with 'AIzaSy' configured)");
            return generateMockAnalysis(incident);
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            ));

            String url = String.format("%s/%s:generateContent?key=%s",
                    aiConfig.getApiUrl(),
                    aiConfig.getModel(),
                    aiConfig.getApiKey());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                // Extract Gemini response text
                String text = root.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text")
                        .asText();
                
                if (text != null && !text.isBlank()) {
                    return parseAiOutput(text);
                }
            } else {
                logger.warn("AI API request failed with status code {}. Body: {}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            logger.error("Exception during AI analysis call: {}", e.getMessage(), e);
        }

        // Return high-quality contextual mock summary on API failure
        logger.info("Falling back to simulated AI analyst report");
        return generateMockAnalysis(incident);
    }

    private String buildPrompt(Incident incident) {
        String alertsStr = incident.getAlerts().stream()
                .map(a -> String.format("- Title: %s, Threat Type: %s, Severity: %s, Source IP: %s, Details: %s",
                        a.getTitle(), a.getThreatType(), a.getSeverity(), a.getSourceIp(), a.getDescription()))
                .collect(Collectors.joining("\n"));

        return "You are an expert Cloud-Native Security Analyst at Amazon Security Operations Center.\n" +
                "Analyze the following security incident and generate a response formatted EXACTLY as a JSON object with the following keys:\n" +
                "\"incidentSummary\", \"rootCause\", \"recommendations\", \"executiveSummary\".\n" +
                "Ensure values are comprehensive, markdown-friendly text.\n\n" +
                "Incident Title: " + incident.getTitle() + "\n" +
                "Overall Severity: " + incident.getSeverity() + "\n" +
                "Risk Score: " + incident.getRiskScore() + "/100\n" +
                "Correlated Alerts:\n" + alertsStr + "\n\n" +
                "Provide detailed, specific remediation steps matching the threat signatures.";
    }

    private AiReportResponseDto parseAiOutput(String text) {
        try {
            // Trim the response block markdown tags if present (e.g. ```json ... ```)
            String cleanJson = text;
            if (cleanJson.contains("```json")) {
                cleanJson = cleanJson.substring(cleanJson.indexOf("```json") + 7);
                if (cleanJson.contains("```")) {
                    cleanJson = cleanJson.substring(0, cleanJson.indexOf("```"));
                }
            } else if (cleanJson.contains("```")) {
                cleanJson = cleanJson.substring(cleanJson.indexOf("```") + 3);
                if (cleanJson.contains("```")) {
                    cleanJson = cleanJson.substring(0, cleanJson.indexOf("```"));
                }
            }
            cleanJson = cleanJson.trim();

            JsonNode node = objectMapper.readTree(cleanJson);
            return AiReportResponseDto.builder()
                    .incidentSummary(node.path("incidentSummary").asText("Failed to parse AI Summary."))
                    .rootCause(node.path("rootCause").asText("Failed to parse AI Root Cause."))
                    .recommendations(node.path("recommendations").asText("Failed to parse AI Recommendations."))
                    .executiveSummary(node.path("executiveSummary").asText("Failed to parse AI Executive Summary."))
                    .build();
        } catch (Exception e) {
            logger.warn("Failed to parse AI output JSON: {}. Full text: {}", e.getMessage(), text);
            // Fallback parse if the LLM output was text instead of JSON
            return AiReportResponseDto.builder()
                    .incidentSummary("Security Incident Review. Raw Output: " + text.substring(0, Math.min(200, text.length())) + "...")
                    .rootCause("Multiple security triggers.")
                    .recommendations("Review firewall rules and reset user credentials.")
                    .executiveSummary("Aggregated critical indicators of compromise require analyst review.")
                    .build();
        }
    }

    private AiReportResponseDto generateMockAnalysis(Incident incident) {
        StringBuilder summary = new StringBuilder();
        StringBuilder rootCause = new StringBuilder();
        StringBuilder recs = new StringBuilder();
        StringBuilder exec = new StringBuilder();

        boolean isPortScan = incident.getAlerts().stream().anyMatch(a -> "PORT_SCAN".equals(a.getThreatType()));
        boolean isBruteForce = incident.getAlerts().stream().anyMatch(a -> "BRUTE_FORCE".equals(a.getThreatType()));
        boolean isThreatIntel = incident.getAlerts().stream().anyMatch(a -> "THREAT_INTEL".equals(a.getThreatType()));
        boolean isPrivilegeEscalation = incident.getAlerts().stream().anyMatch(a -> "PRIVILEGE_ESCALATION".equals(a.getThreatType()));

        summary.append(String.format("This security incident '%s' with a risk score of %d/100 represents a correlated sequence of events. ",
                incident.getTitle(), incident.getRiskScore()));

        if (isPortScan) {
            summary.append("An external actor has conducted network mapping and reconnaissance against system boundaries. ");
            rootCause.append("### Reconnaissance Activity\nAn external host scanned multiple destination ports, looking for open services and entry points. This is standard preparatory behavior before an attack vector is launched.\n\n");
            recs.append("### Port Scan Remediation\n1. **IP Isolation**: Block the source IP in AWS Security Groups and Network Access Control Lists (NACLs).\n2. **Close Unused Ports**: Audit external facing services and shut down any ports not strictly necessary.\n3. **IDS/IPS Rules**: Configure automatic rule sets to blacklist IPs running scans.\n\n");
        }
        if (isBruteForce) {
            summary.append("Multiple failed login attempts indicate an active authentication bypass attempt targeting administrative services. ");
            rootCause.append("### Authentication Brute Force\nRepetitive failed authorization requests suggest a dictionary or credential stuffing attack targeting system endpoints.\n\n");
            recs.append("### Authentication Remediation\n1. **Account Lockout**: Implement strong account lockout thresholds (e.g. max 5 failures).\n2. **MFA Enforce**: Mandate Multi-Factor Authentication for all users.\n3. **API Rate Limiting**: Ensure Redis rate limiters are active on endpoints.\n\n");
        }
        if (isThreatIntel) {
            summary.append("Inbound/outbound traffic has been identified involving hosts flagged in active threat feeds. ");
            rootCause.append("### Threat Intelligence Match\nThe system registered interactions with an IP listed in malicious server databases, indicating potential command-and-control communication.\n\n");
            recs.append("### Threat Feed Remediation\n1. **NACL Block**: Create an explicit DENY rule in the subnet's NACL for the malicious IP.\n2. **Inspect Host**: Run malware scans on internal servers communicating with this destination.\n\n");
        }
        if (isPrivilegeEscalation) {
            summary.append("The threat engine flagged actions suggesting an unauthorized attempt to upgrade permissions or modify administrative groups. ");
            rootCause.append("### Privilege Escalation Attempt\nA user attempted to execute unauthorized root access privileges, indicating possible compromise of basic privileges or insider threat.\n\n");
            recs.append("### Permission Remediation\n1. **Revoke Access**: Suspend the target user session immediately.\n2. **IAM Audit**: Audit AWS IAM roles and ensure the Principle of Least Privilege is strictly enforced.\n\n");
        }

        if (incident.getAlerts().isEmpty()) {
            summary.append("No active alerts are currently correlated with this incident.");
            rootCause.append("No root cause could be automatically determined. No alerts linked.");
            recs.append("Audit log trails manually and verify ingress network configurations.");
        }

        exec.append(String.format("### Executive Summary Report\n\n**Severity Level:** %s  \n**Risk Score:** %d/100  \n\n" +
                        "**Operational Impact:** Moderate to High depending on server exposures. The Threat Engine detected correlated triggers suggesting %s targeting platform infrastructure. Immediate mitigation is advised to safeguard internal assets.",
                incident.getSeverity(), incident.getRiskScore(), 
                incident.getAlerts().stream().map(Alert::getThreatType).collect(Collectors.joining(", "))));

        return AiReportResponseDto.builder()
                .incidentSummary(summary.toString())
                .rootCause(rootCause.toString())
                .recommendations(recs.toString())
                .executiveSummary(exec.toString())
                .build();
    }
}

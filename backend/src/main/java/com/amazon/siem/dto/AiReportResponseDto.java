package com.amazon.siem.dto;

public class AiReportResponseDto {
    private String incidentSummary;
    private String rootCause;
    private String recommendations;
    private String executiveSummary;

    public AiReportResponseDto() {}

    public AiReportResponseDto(String incidentSummary, String rootCause, String recommendations, String executiveSummary) {
        this.incidentSummary = incidentSummary;
        this.rootCause = rootCause;
        this.recommendations = recommendations;
        this.executiveSummary = executiveSummary;
    }

    public String getIncidentSummary() {
        return incidentSummary;
    }

    public void setIncidentSummary(String incidentSummary) {
        this.incidentSummary = incidentSummary;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public static AiReportResponseDtoBuilder builder() {
        return new AiReportResponseDtoBuilder();
    }

    public static class AiReportResponseDtoBuilder {
        private String incidentSummary;
        private String rootCause;
        private String recommendations;
        private String executiveSummary;

        public AiReportResponseDtoBuilder incidentSummary(String incidentSummary) {
            this.incidentSummary = incidentSummary;
            return this;
        }

        public AiReportResponseDtoBuilder rootCause(String rootCause) {
            this.rootCause = rootCause;
            return this;
        }

        public AiReportResponseDtoBuilder recommendations(String recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public AiReportResponseDtoBuilder executiveSummary(String executiveSummary) {
            this.executiveSummary = executiveSummary;
            return this;
        }

        public AiReportResponseDto build() {
            return new AiReportResponseDto(incidentSummary, rootCause, recommendations, executiveSummary);
        }
    }
}

package com.amazon.siem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${ai.provider:gemini}")
    private String provider;

    @Value("${ai.api-key:mock_ai_key}")
    private String apiKey;

    @Value("${ai.model:gemini-1.5-flash}")
    private String model;

    @Value("${ai.api-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    public AiConfig() {}

    public String getProvider() {
        return provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}

package com.example.bridgeservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for external application URLs and settings
 */
@Component
@ConfigurationProperties(prefix = "bridge.services")
public class ApplicationProperties {

    private ServiceConfig applicationA = new ServiceConfig();
    private ServiceConfig applicationB = new ServiceConfig();
    private int timeoutMillis = 5000;
    private int maxRetries = 3;

    public ServiceConfig getApplicationA() {
        return applicationA;
    }

    public void setApplicationA(ServiceConfig applicationA) {
        this.applicationA = applicationA;
    }

    public ServiceConfig getApplicationB() {
        return applicationB;
    }

    public void setApplicationB(ServiceConfig applicationB) {
        this.applicationB = applicationB;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public static class ServiceConfig {
        private String baseUrl;
        private String apiKey;
        private boolean enabled = true;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

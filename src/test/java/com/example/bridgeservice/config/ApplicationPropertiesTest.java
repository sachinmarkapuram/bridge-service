package com.example.bridgeservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ApplicationProperties configuration
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationPropertiesTest {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Test
    void applicationPropertiesShouldLoadCorrectly() {
        assertThat(applicationProperties).isNotNull();
        assertThat(applicationProperties.getApplicationA()).isNotNull();
        assertThat(applicationProperties.getApplicationB()).isNotNull();
    }

    @Test
    void serviceConfigurationShouldBeLoaded() {
        assertThat(applicationProperties.getApplicationA()).isNotNull();
        assertThat(applicationProperties.getApplicationB()).isNotNull();
    }

    @Test
    void testEnvironmentConfigurationShouldHaveTestValues() {
        // Test configuration should have test-specific values
        assertThat(applicationProperties.getTimeoutMillis()).isEqualTo(1000);
        assertThat(applicationProperties.getMaxRetries()).isEqualTo(1);
    }

    @Test
    void applicationServicesShouldBeEnabled() {
        assertThat(applicationProperties.getApplicationA().isEnabled()).isTrue();
        assertThat(applicationProperties.getApplicationB().isEnabled()).isTrue();
    }

    @Test
    void applicationServicesShouldHaveTestApiKeys() {
        assertThat(applicationProperties.getApplicationA().getApiKey()).isEqualTo("test-api-key-a");
        assertThat(applicationProperties.getApplicationB().getApiKey()).isEqualTo("test-api-key-b");
    }
}

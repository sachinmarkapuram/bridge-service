package com.example.bridgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Bridge Service
 * This test runs the full Spring Boot application and tests the actual endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
public class BridgeServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Test
    public void contextLoads() {
        // Test that the Spring context loads successfully
    }

    @Test
    public void actuatorHealthEndpointShouldReturnUp() {
        String url = createURLWithPort("/actuator/health");
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    public void actuatorInfoEndpointShouldBeAccessible() {
        String url = createURLWithPort("/actuator/info");
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void actuatorCircuitbreakersEndpointShouldReturnCircuitBreakerInfo() {
        String url = createURLWithPort("/actuator/circuitbreakers");
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("circuitBreakers");
    }

    @Test
    public void bridgeHealthEndpointShouldReturnHealthStatus() {
        String url = createURLWithPort("/api/bridge/health");
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // The response might be DOWN since external services are not available
        // but the endpoint should be reachable
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void bridgePingEndpointShouldReturnPong() {
        String url = createURLWithPort("/api/bridge/ping");
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("pong");
    }

    @Test
    public void bridgeInfoEndpointShouldReturnServiceInfo() {
        String url = createURLWithPort("/api/bridge/info");
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("bridge-service");
    }
}

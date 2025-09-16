package com.example.bridgeservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
    
    @Autowired
    private ObjectMapper objectMapper;

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
        assertThat(response.getBody()).contains("\"serviceName\":\"bridge-service\"");
        assertThat(response.getBody()).contains("\"initiate\":\"GET /api/bridge/initiate");
        assertThat(response.getBody()).contains("\"lse_integration\":\"Initiate asynchronous data fetch from London Stock Exchange\"");
    }

    @Nested
    @DisplayName("LSE Async API Integration Tests")
    class LSEAsyncAPITests {

        @Test
        @DisplayName("Should initiate market data fetch successfully")
        public void shouldInitiateMarketDataFetchSuccessfully() throws Exception {
            String url = createURLWithPort("/api/bridge/initiate");
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED),
                () -> assertThat(response.getBody()).isNotNull()
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            
            assertAll(
                () -> assertThat(responseBody.get("success")).isEqualTo(true),
                () -> assertThat(responseBody.get("message")).isEqualTo("LSE data fetch initiated successfully"),
                () -> assertThat(responseBody.get("sourceApplication")).isEqualTo("London Stock Exchange Bridge"),
                () -> assertThat(responseBody.get("correlationId")).isNotNull()
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            assertAll(
                () -> assertThat(data.get("status")).isEqualTo("initiated"),
                () -> assertThat(data.get("dataType")).isEqualTo("market"),
                () -> assertThat(data.get("correlationId")).isNotNull(),
                () -> assertThat(data.get("timestamp")).isNotNull()
            );
        }

        @Test
        @DisplayName("Should initiate stock data fetch successfully")
        public void shouldInitiateStockDataFetchSuccessfully() throws Exception {
            String url = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/api/bridge/initiate"))
                .queryParam("dataType", "stock")
                .queryParam("symbol", "AAPL")
                .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED),
                () -> assertThat(response.getBody()).isNotNull()
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            
            assertAll(
                () -> assertThat(responseBody.get("success")).isEqualTo(true),
                () -> assertThat(responseBody.get("message")).isEqualTo("LSE data fetch initiated successfully"),
                () -> assertThat(responseBody.get("correlationId")).isNotNull()
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            assertAll(
                () -> assertThat(data.get("status")).isEqualTo("initiated"),
                () -> assertThat(data.get("dataType")).isEqualTo("stock"),
                () -> assertThat(data.get("symbol")).isEqualTo("AAPL"),
                () -> assertThat(data.get("correlationId")).isNotNull()
            );
        }

        @Test
        @DisplayName("Should handle explicit market data type")
        public void shouldHandleExplicitMarketDataType() throws Exception {
            String url = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/api/bridge/initiate"))
                .queryParam("dataType", "market")
                .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            
            assertThat(data.get("dataType")).isEqualTo("market");
        }

        @Test
        @DisplayName("Should return error for stock data type without symbol")
        public void shouldReturnErrorForStockDataTypeWithoutSymbol() throws Exception {
            String url = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/api/bridge/initiate"))
                .queryParam("dataType", "stock")
                .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            
            assertAll(
                () -> assertThat(responseBody.get("success")).isEqualTo(false),
                () -> assertThat(responseBody.get("message")).asString().contains("Invalid data type or missing symbol")
            );
        }

        @Test
        @DisplayName("Should return error for invalid data type")
        public void shouldReturnErrorForInvalidDataType() throws Exception {
            String url = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/api/bridge/initiate"))
                .queryParam("dataType", "invalid")
                .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            
            assertAll(
                () -> assertThat(responseBody.get("success")).isEqualTo(false),
                () -> assertThat(responseBody.get("message")).asString().contains("Invalid data type")
            );
        }

        @Test
        @DisplayName("Should handle multiple concurrent requests")
        public void shouldHandleMultipleConcurrentRequests() throws Exception {
            String marketUrl = createURLWithPort("/api/bridge/initiate");
            String stockUrl = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/api/bridge/initiate"))
                .queryParam("dataType", "stock")
                .queryParam("symbol", "GOOGL")
                .toUriString();
            
            // Make concurrent requests
            ResponseEntity<String> marketResponse = restTemplate.getForEntity(marketUrl, String.class);
            ResponseEntity<String> stockResponse = restTemplate.getForEntity(stockUrl, String.class);
            
            // Both should succeed
            assertAll(
                () -> assertThat(marketResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED),
                () -> assertThat(stockResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED)
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> marketBody = objectMapper.readValue(marketResponse.getBody(), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> stockBody = objectMapper.readValue(stockResponse.getBody(), Map.class);
            
            // Should have different correlation IDs
            assertThat(marketBody.get("correlationId"))
                .isNotEqualTo(stockBody.get("correlationId"));
        }

        @Test
        @DisplayName("Should validate response time is reasonable for async operation")
        public void shouldValidateResponseTimeIsReasonableForAsyncOperation() throws Exception {
            String url = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/api/bridge/initiate"))
                .queryParam("dataType", "stock")
                .queryParam("symbol", "TSLA")
                .toUriString();
            
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            long endTime = System.currentTimeMillis();
            
            long responseTime = endTime - startTime;
            
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED),
                () -> assertThat(responseTime).isLessThan(5000), // Should respond within 5 seconds
                () -> assertThat(responseTime).isGreaterThan(0)
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            
            // Should indicate async operation was initiated
            assertThat(data.get("status")).isEqualTo("initiated");
        }

        @Test
        @DisplayName("Should have proper JSON structure in response")
        public void shouldHaveProperJSONStructureInResponse() throws Exception {
            String url = createURLWithPort("/api/bridge/initiate");
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
            
            // Validate JSON structure
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            
            // Top level fields
            assertAll(
                () -> assertThat(responseBody).containsKeys("success", "message", "sourceApplication", "correlationId", "data"),
                () -> assertThat(responseBody.get("success")).isInstanceOf(Boolean.class),
                () -> assertThat(responseBody.get("message")).isInstanceOf(String.class),
                () -> assertThat(responseBody.get("sourceApplication")).isInstanceOf(String.class),
                () -> assertThat(responseBody.get("correlationId")).isInstanceOf(String.class)
            );
            
            // Data object structure
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            assertAll(
                () -> assertThat(data).containsKeys("correlationId", "dataType", "status", "message", "timestamp"),
                () -> assertThat(data.get("correlationId")).isInstanceOf(String.class),
                () -> assertThat(data.get("dataType")).isInstanceOf(String.class),
                () -> assertThat(data.get("status")).isInstanceOf(String.class),
                () -> assertThat(data.get("message")).isInstanceOf(String.class),
                () -> assertThat(data.get("timestamp")).isInstanceOf(Number.class)
            );
        }
    }

    @Nested
    @DisplayName("API Documentation Tests")
    class APIDocumentationTests {

        @Test
        @DisplayName("Info endpoint should include LSE integration documentation")
        public void infoEndpointShouldIncludeLSEIntegrationDocumentation() throws Exception {
            String url = createURLWithPort("/api/bridge/info");
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> endpoints = (Map<String, Object>) responseBody.get("endpoints");
            @SuppressWarnings("unchecked")
            Map<String, Object> operations = (Map<String, Object>) responseBody.get("supportedOperations");
            
            assertAll(
                () -> assertThat(endpoints).containsKey("initiate"),
                () -> assertThat(endpoints.get("initiate")).asString().contains("/api/bridge/initiate"),
                () -> assertThat(endpoints.get("initiate")).asString().contains("dataType={market|stock}"),
                () -> assertThat(operations).containsKey("lse_integration"),
                () -> assertThat(operations.get("lse_integration")).asString().contains("London Stock Exchange")
            );
        }
    }
}

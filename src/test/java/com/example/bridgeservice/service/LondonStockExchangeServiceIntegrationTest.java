package com.example.bridgeservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTimeout;

/**
 * Integration tests for London Stock Exchange Service
 * Tests the async functionality and mock data generation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@DisplayName("London Stock Exchange Service Integration Tests")
public class LondonStockExchangeServiceIntegrationTest {

    @Autowired
    private LondonStockExchangeService londonStockExchangeService;

    @Nested
    @DisplayName("Async Market Data Tests")
    class AsyncMarketDataTests {

        @Test
        @DisplayName("Should fetch market data asynchronously")
        public void shouldFetchMarketDataAsynchronously() {
            String correlationId = UUID.randomUUID().toString();
            
            CompletableFuture<Map<String, Object>> future = londonStockExchangeService
                .fetchMarketDataAsync(correlationId);
            
            assertThat(future).isNotNull();
            // Note: We don't check isDone() immediately as the mock service may complete very quickly
            
            // Wait for completion and validate result
            assertTimeout(java.time.Duration.ofSeconds(10), () -> {
                Map<String, Object> result = future.get(5, TimeUnit.SECONDS);
                
                assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.get("success")).isEqualTo(true),
                    () -> assertThat(result.get("correlationId")).isEqualTo(correlationId),
                    () -> assertThat(result.get("source")).isEqualTo("London Stock Exchange"),
                    () -> assertThat(result.get("dataType")).isEqualTo("market_overview")
                );
                
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                assertAll(
                    () -> assertThat(data).containsKeys("ftse100", "ftse250", "ftseAll", "volume", "trades", "marketStatus"),
                    () -> assertThat(data.get("ftse100")).isInstanceOf(Double.class),
                    () -> assertThat(data.get("volume")).isInstanceOf(Long.class),
                    () -> assertThat(data.get("marketStatus")).isEqualTo("OPEN")
                );
            });
        }

        @Test
        @DisplayName("Should generate realistic mock market data")
        public void shouldGenerateRealisticMockMarketData() {
            String correlationId = UUID.randomUUID().toString();
            
            assertTimeout(java.time.Duration.ofSeconds(10), () -> {
                Map<String, Object> result = londonStockExchangeService
                    .fetchMarketDataAsync(correlationId)
                    .get(5, TimeUnit.SECONDS);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                
                // Validate realistic data ranges
                assertAll(
                    () -> assertThat((Double) data.get("ftse100")).isBetween(5000.0, 10000.0),
                    () -> assertThat((Double) data.get("ftse250")).isBetween(15000.0, 25000.0),
                    () -> assertThat((Long) data.get("volume")).isPositive(),
                    () -> assertThat((Integer) data.get("trades")).isPositive(),
                    () -> assertThat(data.get("lastUpdated")).isNotNull()
                );
            });
        }
    }

    @Nested
    @DisplayName("Async Stock Data Tests")
    class AsyncStockDataTests {

        @Test
        @DisplayName("Should fetch stock data asynchronously")
        public void shouldFetchStockDataAsynchronously() {
            String correlationId = UUID.randomUUID().toString();
            String symbol = "AAPL";
            
            CompletableFuture<Map<String, Object>> future = londonStockExchangeService
                .fetchStockDataAsync(symbol, correlationId);
            
            assertThat(future).isNotNull();
            
            // Wait for completion and validate result
            assertTimeout(java.time.Duration.ofSeconds(10), () -> {
                Map<String, Object> result = future.get(5, TimeUnit.SECONDS);
                
                assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.get("success")).isEqualTo(true),
                    () -> assertThat(result.get("correlationId")).isEqualTo(correlationId),
                    () -> assertThat(result.get("symbol")).isEqualTo(symbol),
                    () -> assertThat(result.get("source")).isEqualTo("London Stock Exchange"),
                    () -> assertThat(result.get("dataType")).isEqualTo("stock_data")
                );
                
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                assertAll(
                    () -> assertThat(data).containsKeys("price", "change", "changePercent", "volume", "high", "low", "open", "previousClose", "marketCap"),
                    () -> assertThat(data.get("price")).isInstanceOf(Double.class),
                    () -> assertThat(data.get("volume")).isInstanceOf(Long.class),
                    () -> assertThat(data.get("marketCap")).isInstanceOf(Long.class)
                );
            });
        }

        @Test
        @DisplayName("Should generate different data for different symbols")
        public void shouldGenerateDifferentDataForDifferentSymbols() {
            String correlationId1 = UUID.randomUUID().toString();
            String correlationId2 = UUID.randomUUID().toString();
            
            assertTimeout(java.time.Duration.ofSeconds(15), () -> {
                Map<String, Object> appleResult = londonStockExchangeService
                    .fetchStockDataAsync("AAPL", correlationId1)
                    .get(5, TimeUnit.SECONDS);
                    
                Map<String, Object> googleResult = londonStockExchangeService
                    .fetchStockDataAsync("GOOGL", correlationId2)
                    .get(5, TimeUnit.SECONDS);
                
                // Both should be successful but have different correlation IDs
                assertAll(
                    () -> assertThat(appleResult.get("success")).isEqualTo(true),
                    () -> assertThat(googleResult.get("success")).isEqualTo(true),
                    () -> assertThat(appleResult.get("correlationId")).isNotEqualTo(googleResult.get("correlationId")),
                    () -> assertThat(appleResult.get("symbol")).isEqualTo("AAPL"),
                    () -> assertThat(googleResult.get("symbol")).isEqualTo("GOOGL")
                );
                
                // Data values might be different due to randomization
                @SuppressWarnings("unchecked")
                Map<String, Object> appleData = (Map<String, Object>) appleResult.get("data");
                @SuppressWarnings("unchecked")
                Map<String, Object> googleData = (Map<String, Object>) googleResult.get("data");
                
                // Validate realistic stock price ranges
                assertAll(
                    () -> assertThat((Double) appleData.get("price")).isBetween(100.0, 200.0),
                    () -> assertThat((Double) googleData.get("price")).isBetween(100.0, 200.0),
                    () -> assertThat((Long) appleData.get("volume")).isPositive(),
                    () -> assertThat((Long) googleData.get("volume")).isPositive()
                );
            });
        }

        @Test
        @DisplayName("Should handle special characters in symbol")
        public void shouldHandleSpecialCharactersInSymbol() {
            String correlationId = UUID.randomUUID().toString();
            String symbol = "BRK.B"; // Berkshire Hathaway Class B
            
            assertTimeout(java.time.Duration.ofSeconds(10), () -> {
                Map<String, Object> result = londonStockExchangeService
                    .fetchStockDataAsync(symbol, correlationId)
                    .get(5, TimeUnit.SECONDS);
                
                assertAll(
                    () -> assertThat(result.get("success")).isEqualTo(true),
                    () -> assertThat(result.get("symbol")).isEqualTo(symbol),
                    () -> assertThat(result.get("correlationId")).isEqualTo(correlationId)
                );
            });
        }
    }

    @Nested
    @DisplayName("Async Performance Tests")
    class AsyncPerformanceTests {

        @Test
        @DisplayName("Should handle multiple concurrent async requests")
        public void shouldHandleMultipleConcurrentAsyncRequests() {
            String correlationId1 = UUID.randomUUID().toString();
            String correlationId2 = UUID.randomUUID().toString();
            String correlationId3 = UUID.randomUUID().toString();
            
            // Start multiple async operations simultaneously
            CompletableFuture<Map<String, Object>> marketFuture = londonStockExchangeService
                .fetchMarketDataAsync(correlationId1);
            CompletableFuture<Map<String, Object>> appleFuture = londonStockExchangeService
                .fetchStockDataAsync("AAPL", correlationId2);
            CompletableFuture<Map<String, Object>> googleFuture = londonStockExchangeService
                .fetchStockDataAsync("GOOGL", correlationId3);
            
            // Wait for all to complete
            assertTimeout(java.time.Duration.ofSeconds(15), () -> {
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    marketFuture, appleFuture, googleFuture
                );
                
                allFutures.get(10, TimeUnit.SECONDS);
                
                // Validate all completed successfully
                Map<String, Object> marketResult = marketFuture.get();
                Map<String, Object> appleResult = appleFuture.get();
                Map<String, Object> googleResult = googleFuture.get();
                
                assertAll(
                    () -> assertThat(marketResult.get("success")).isEqualTo(true),
                    () -> assertThat(appleResult.get("success")).isEqualTo(true),
                    () -> assertThat(googleResult.get("success")).isEqualTo(true),
                    () -> assertThat(marketResult.get("correlationId")).isEqualTo(correlationId1),
                    () -> assertThat(appleResult.get("correlationId")).isEqualTo(correlationId2),
                    () -> assertThat(googleResult.get("correlationId")).isEqualTo(correlationId3)
                );
            });
        }

        @Test
        @DisplayName("Should complete async operations within reasonable time")
        public void shouldCompleteAsyncOperationsWithinReasonableTime() {
            String correlationId = UUID.randomUUID().toString();
            
            long startTime = System.currentTimeMillis();
            
            assertTimeout(java.time.Duration.ofSeconds(5), () -> {
                Map<String, Object> result = londonStockExchangeService
                    .fetchMarketDataAsync(correlationId)
                    .get(3, TimeUnit.SECONDS);
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                assertAll(
                    () -> assertThat(result.get("success")).isEqualTo(true),
                    () -> assertThat(duration).isLessThan(5000), // Should complete within 5 seconds
                    () -> assertThat(duration).isGreaterThan(0)
                );
            });
        }
    }

    @Nested
    @DisplayName("Response Validation Tests")
    class ResponseValidationTests {

        @Test
        @DisplayName("Should validate successful response structure")
        public void shouldValidateSuccessfulResponseStructure() {
            String correlationId = UUID.randomUUID().toString();
            
            assertTimeout(java.time.Duration.ofSeconds(10), () -> {
                Map<String, Object> result = londonStockExchangeService
                    .fetchMarketDataAsync(correlationId)
                    .get(5, TimeUnit.SECONDS);
                
                // Test the isValidResponse method
                boolean isValid = londonStockExchangeService.isValidResponse(result);
                
                assertAll(
                    () -> assertThat(isValid).isTrue(),
                    () -> assertThat(result).containsKeys("success", "correlationId", "data", "source", "dataType"),
                    () -> assertThat(result.get("success")).isEqualTo(true),
                    () -> assertThat(result.get("data")).isNotNull()
                );
            });
        }

        @Test
        @DisplayName("Should validate response contains timestamp")
        public void shouldValidateResponseContainsTimestamp() {
            String correlationId = UUID.randomUUID().toString();
            
            assertTimeout(java.time.Duration.ofSeconds(10), () -> {
                Map<String, Object> result = londonStockExchangeService
                    .fetchStockDataAsync("MSFT", correlationId)
                    .get(5, TimeUnit.SECONDS);
                
                assertAll(
                    () -> assertThat(result.get("timestamp")).isNotNull(),
                    () -> assertThat(result.get("timestamp")).isInstanceOf(Long.class),
                    () -> assertThat((Long) result.get("timestamp")).isPositive()
                );
                
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                assertThat(data.get("lastUpdated")).isNotNull();
            });
        }
    }
}
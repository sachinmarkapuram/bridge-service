package com.example.bridgeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestTemplate;

/**
 * Main Spring Boot application class for the Bridge Service.
 * This application acts as an intermediary between two stateless applications.
 */
@SpringBootApplication
@EnableConfigurationProperties
public class BridgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BridgeServiceApplication.class, args);
    }

    /**
     * Bean for traditional blocking HTTP client
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Bean for reactive HTTP client with connection pooling
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Default WebClient bean
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}

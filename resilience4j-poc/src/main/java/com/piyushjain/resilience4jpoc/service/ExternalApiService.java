package com.piyushjain.resilience4jpoc.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Slf4j
@Service
public class ExternalApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Retry(name = "externalApiRetry", fallbackMethod = "fallback")
    @CircuitBreaker(name = "externalApiCircuitBreaker", fallbackMethod = "fallback")
    public String callExternalApi() {

        log.info("Calling external API...");

        return restTemplate.getForObject(
                "http://localhost:8080/external/data",
                String.class
        );
    }

    public String fallback(Exception ex) {
        log.error("Fallback executed due to: {}", ex.getMessage());
        return "External Api is UnHealthy will try after some time.";
    }
}
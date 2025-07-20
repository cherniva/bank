package com.cherniva.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RetryConfig {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                        .minimumNumberOfCalls(5)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .failureRateThreshold(50.0f)
                        .recordException(throwable -> 
                                throwable instanceof java.net.ConnectException ||
                                throwable instanceof java.net.SocketTimeoutException ||
                                throwable instanceof java.io.IOException ||
                                throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException
                        )
                        .build())
                .build());
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> criticalServicesCustomizer() {
        return factory -> {
            // Custom configuration for critical financial services (transfer, cash, accounts)
            factory.configure(builder -> builder
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(15)
                            .permittedNumberOfCallsInHalfOpenState(5)
                            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                            .minimumNumberOfCalls(10)
                            .waitDurationInOpenState(Duration.ofSeconds(15))
                            .failureRateThreshold(30.0f) // More strict threshold for critical services
                            .build())
                    .build(), 
                    "transfer-service-cb", "cash-service-cb", "accounts-service-cb");

            // Custom configuration for notification service (less critical)
            factory.configure(builder -> builder
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(5)
                            .permittedNumberOfCallsInHalfOpenState(2)
                            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                            .minimumNumberOfCalls(3)
                            .waitDurationInOpenState(Duration.ofSeconds(5))
                            .failureRateThreshold(70.0f) // More lenient threshold
                            .build())
                    .build(), 
                    "notifications-service-cb");

            // Custom configuration for blocker service (fast fail)
            factory.configure(builder -> builder
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(5)
                            .permittedNumberOfCallsInHalfOpenState(2)
                            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                            .minimumNumberOfCalls(3)
                            .waitDurationInOpenState(Duration.ofSeconds(3))
                            .failureRateThreshold(60.0f)
                            .build())
                    .build(), 
                    "blocker-service-cb");
        };
    }
} 
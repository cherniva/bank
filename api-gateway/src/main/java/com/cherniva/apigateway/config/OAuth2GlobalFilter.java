package com.cherniva.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OAuth2GlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveOAuth2AuthorizedClientManager clientManager;
    private final ServiceAuthConfig serviceAuthConfig;

    public OAuth2GlobalFilter(ReactiveOAuth2AuthorizedClientManager clientManager, 
                             ServiceAuthConfig serviceAuthConfig) {
        this.clientManager = clientManager;
        this.serviceAuthConfig = serviceAuthConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Check if this request should skip authentication
        if (shouldSkipAuth(path)) {
            return chain.filter(exchange);
        }
        
//        // Check if this request should use client credentials flow
//        String clientRegistrationId = getClientRegistrationId(path);
        String clientRegistrationId = "gateway-client"; // use defaul
        if (clientRegistrationId != null) {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientRegistrationId)
                    .principal(clientRegistrationId)
                    .build();

            return clientManager.authorize(authorizeRequest)
                    .flatMap(client -> {
                        String token = client.getAccessToken().getTokenValue();
                        
                        // Create a new request with the Authorization header
                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(exchange.getRequest().mutate()
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                        .build())
                                .build();
                        
                        return chain.filter(mutatedExchange);
                    });
        }
        
        return chain.filter(exchange);
    }
    
    /**
     * Determines if a request path should skip authentication entirely.
     */
    private boolean shouldSkipAuth(String path) {
        return serviceAuthConfig.getSkipAuthPaths().stream()
                .anyMatch(path::startsWith);
    }
    
    /**
     * Gets the OAuth2 client registration ID for a given path.
     * Returns null if no client credentials should be used for this path.
     */
    private String getClientRegistrationId(String path) {
        Map<String, String> mappings = serviceAuthConfig.getServiceClientMappings();
        
        // First, try to find an exact path mapping
        String clientId = mappings.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        
        // If no specific mapping found, use default for API paths
        if (clientId == null && path.startsWith("/api/")) {
            clientId = serviceAuthConfig.getDefaultClientRegistrationId();
        }
        
        return clientId;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1000; // Run after other filters
    }
} 
package com.cherniva.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "gateway.auth")
public class ServiceAuthConfig {
    
    /**
     * Service path mappings to OAuth2 client registration IDs
     * Example: /api/accounts/ -> gateway-client
     */
    private Map<String, String> serviceClientMappings = new HashMap<>();
    
    /**
     * Paths that should skip authentication entirely
     */
    private Set<String> skipAuthPaths = new HashSet<>();
    
    /**
     * Default OAuth2 client registration ID to use if no specific mapping is found
     */
    private String defaultClientRegistrationId = "gateway-client";
    
    public ServiceAuthConfig() {
        // Set default mappings
        serviceClientMappings.put("/api/accounts/", "gateway-client");
        serviceClientMappings.put("/accounts/", "gateway-client");
        serviceClientMappings.put("/api/users/", "gateway-client");
        serviceClientMappings.put("/users/", "gateway-client");
        serviceClientMappings.put("/api/transactions/", "gateway-client");
        serviceClientMappings.put("/transactions/", "gateway-client");
        serviceClientMappings.put("/api/notifications/", "gateway-client");
        serviceClientMappings.put("/notifications/", "gateway-client");
        serviceClientMappings.put("/exchange/course", "gateway-client");
        serviceClientMappings.put("/exchange/course/update", "gateway-client");
        
        // Set default skip auth paths
        skipAuthPaths.add("/auth/");
        skipAuthPaths.add("/public/");
        skipAuthPaths.add("/actuator/");
        skipAuthPaths.add("/health");
        skipAuthPaths.add("/info");
        skipAuthPaths.add("/swagger-ui/");
        skipAuthPaths.add("/v3/api-docs/");
    }
    
    public Map<String, String> getServiceClientMappings() {
        return serviceClientMappings;
    }
    
    public void setServiceClientMappings(Map<String, String> serviceClientMappings) {
        this.serviceClientMappings = serviceClientMappings;
    }
    
    public Set<String> getSkipAuthPaths() {
        return skipAuthPaths;
    }
    
    public void setSkipAuthPaths(Set<String> skipAuthPaths) {
        this.skipAuthPaths = skipAuthPaths;
    }
    
    public String getDefaultClientRegistrationId() {
        return defaultClientRegistrationId;
    }
    
    public void setDefaultClientRegistrationId(String defaultClientRegistrationId) {
        this.defaultClientRegistrationId = defaultClientRegistrationId;
    }
} 
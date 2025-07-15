package com.cherniva.frontui.service;

import com.cherniva.common.dto.SessionValidationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final RestTemplate restTemplate;
    
    @Value("${accounts.service.name:accounts-service}")
    private String accountsServiceName;

    public SessionValidationDto validateSession(String sessionId) {
        try {
            return restTemplate.postForObject(
                "http://" + accountsServiceName + "/api/auth/validate-session?sessionId=" + sessionId,
                null,
                SessionValidationDto.class
            );
        } catch (Exception e) {
            SessionValidationDto invalidResponse = new SessionValidationDto();
            invalidResponse.setSessionId(sessionId);
            invalidResponse.setValid(false);
            return invalidResponse;
        }
    }

    public void logout(String sessionId) {
        try {
            restTemplate.postForObject(
                "http://" + accountsServiceName + "/api/auth/logout?sessionId=" + sessionId,
                null,
                Void.class
            );
        } catch (Exception e) {
            // Log error but don't throw exception
        }
    }
} 
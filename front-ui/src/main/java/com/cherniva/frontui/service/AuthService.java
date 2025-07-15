package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    
    @Value("${accounts.service.name:accounts-service}")
    private String accountsServiceName;

    public UserAccountResponseDto authenticateUser(UserLoginDto loginDto) {
        try {
            return restTemplate.postForObject(
                "http://" + accountsServiceName + "/api/auth/login", 
                loginDto, 
                UserAccountResponseDto.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }
} 
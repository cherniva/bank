package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RestTemplate restTemplate;
    
    @Value("${accounts.service.name:accounts-service}")
    private String accountsServiceName;

    public UserAccountResponseDto authenticateUser(UserLoginDto loginDto) {
        try {
            log.info("start authentication");
            return restTemplate.postForObject(
                "lb://api-gateway/api/auth/login",
                loginDto, 
                UserAccountResponseDto.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }
} 
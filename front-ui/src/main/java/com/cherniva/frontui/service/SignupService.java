package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.dto.UserRegistrationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto registerUser(UserRegistrationDto registrationDto) {
        try {
            log.info("start registration");
            return restTemplate.postForObject(
                    "lb://api-gateway/api/accounts/register",
                    registrationDto,
                    UserAccountResponseDto.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }
}

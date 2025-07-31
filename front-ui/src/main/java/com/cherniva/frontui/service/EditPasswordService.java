package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserRegistrationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class EditPasswordService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto editPassword(String sessionId, String password) {
        try {
            log.info("edit password");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/users/editPassword")
                    .queryParam("sessionId", sessionId)
                    .queryParam("password", password);

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.POST,
                        requestEntity,
                        UserAccountResponseDto.class)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }
}

package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteAccountService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto deleteAccount(String sessionId, Long accountId) {
        try {
            log.info("delete account with id: {}", accountId);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/accounts/deleteAccount")
                    .queryParam("sessionId", sessionId)
                    .queryParam("accountId", accountId);

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.DELETE,
                            requestEntity,
                            UserAccountResponseDto.class)
                    .getBody();
        } catch (Exception e) {
            log.error("Delete account failed", e);
            throw new RuntimeException("Delete account failed", e);
        }
    }
} 
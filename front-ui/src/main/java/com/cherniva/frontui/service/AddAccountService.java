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
public class AddAccountService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto addAccount(String sessionId, String currencyCode) {
        try {
            log.info("add account");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/accounts/addAccount")
                    .queryParam("sessionId", sessionId)
                    .queryParam("currencyCode", currencyCode);

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.POST,
                            requestEntity,
                            UserAccountResponseDto.class)
                    .getBody();
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException("Authentication failed", e);
        }
    }
}

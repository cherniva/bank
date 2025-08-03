package com.cherniva.cashservice.service;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.UserAccountResponseDto;
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
public class SessionService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto updateSession(String sessionId) {
        try {
            return sendRequest("lb://api-gateway/accounts/session/update", sessionId);
        } catch (Exception e) {
            log.error("Cannot update session", e);
            throw new RuntimeException("Cannot update session");
        }
    }

    private UserAccountResponseDto sendRequest(String url, String sessionId) {
        var builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("sessionId", sessionId);
        log.info("Url: {}", url);
        log.info("SessionId: {}", sessionId);
        var headers = new HttpHeaders();
        var request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.PUT,
                        request,
                        UserAccountResponseDto.class
                )
                .getBody();
    }
}

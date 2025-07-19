package com.cherniva.cashservice.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto deposit(String sessionId, Long accountId, BigDecimal amount) {
        try {
            var valid = validOperation();
            log.info("Operation validity status: {}", valid ? "valid" : "invalid");
            if (valid) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/accounts/deposit")
                        .queryParam("sessionId", sessionId)
                        .queryParam("accountId", accountId)
                        .queryParam("amount", amount);

                HttpHeaders headers = new HttpHeaders();

                HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

                return restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.POST,
                            requestEntity,
                            UserAccountResponseDto.class)
                        .getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    public UserAccountResponseDto withdraw(String sessionId, Long accountId, BigDecimal amount) {
        try {
            var valid = validOperation();
            log.info("Operation validity status: {}", valid ? "valid" : "invalid");
            if (valid) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/accounts/withdraw")
                        .queryParam("sessionId", sessionId)
                        .queryParam("accountId", accountId)
                        .queryParam("amount", amount);

                HttpHeaders headers = new HttpHeaders();

                HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

                return restTemplate.exchange(
                                builder.toUriString(),
                                HttpMethod.POST,
                                requestEntity,
                                UserAccountResponseDto.class)
                        .getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    private Boolean validOperation() {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/blocker/check");

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.GET,
                            requestEntity,
                            Boolean.class)
                    .getBody();
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }
}

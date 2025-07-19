package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto deposit(String sessionId, Long accountId, BigDecimal amount) {
        try {
            log.info("Depositing {} to account {}", amount, accountId);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/cash/deposit")
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
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("Error during deposit operation", e);
            throw new RuntimeException("Deposit operation failed", e);
        }
    }

    public UserAccountResponseDto withdraw(String sessionId, Long accountId, BigDecimal amount) {
        try {
            log.info("Withdrawing {} from account {}", amount, accountId);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/cash/withdraw")
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
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("Error during withdraw operation", e);
            throw new RuntimeException("Withdraw operation failed", e);
        }
    }
} 
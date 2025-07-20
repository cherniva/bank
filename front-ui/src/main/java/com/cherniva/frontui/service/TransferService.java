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
public class TransferService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto transfer(String sessionId, BigDecimal amount, String fromCurrency, 
                                         String toCurrency, String username) {
        try {
            log.info("Transferring {} from {} to {} for user {}", amount, fromCurrency, toCurrency, username);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/transfer")
                    .queryParam("sessionId", sessionId)
                    .queryParam("amount", amount)
                    .queryParam("fromCurrency", fromCurrency)
                    .queryParam("toCurrency", toCurrency)
                    .queryParam("username", username);

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
            log.error("Error during transfer operation", e);
            throw new RuntimeException("Transfer operation failed", e);
        }
    }
} 
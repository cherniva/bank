package com.cherniva.transferservice.service;

import com.cherniva.common.dto.AccountDto;
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
public class AccountService {
    private final RestTemplate restTemplate;

    public AccountDto getAccountByUsernameAndCurrencyCode(String username, String currencyCode) {
        try {
            var builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/accounts")
                    .queryParam("username", username)
                    .queryParam("currencyCode", currencyCode);

            var headers = new HttpHeaders();
            var request = new HttpEntity<>(headers);

            return restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.GET,
                            request,
                            AccountDto.class
                    )
                    .getBody();
        } catch (Exception e) {
            log.error("Unable to get account", e);
            throw new RuntimeException(e);
        }
    }
}

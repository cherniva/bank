package com.cherniva.cashservice.service;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.model.Account;
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
public class SyncService {
    private final RestTemplate restTemplate;
    private final AccountMapper accountMapper;

    public void syncWithdraw(Account account) {
        try {
            var accountDto = accountMapper.accountToAccountDto(account);

            sendRequest("lb://api-gateway/accounts/sync/withdraw", accountDto);
            sendRequest("lb://api-gateway/transfer/sync/withdraw", accountDto);
        } catch (Exception e) {
            log.error("Cannot sync withdraw", e);
        }
    }

    public void syncDeposit(Account account) {
        try {
            var accountDto = accountMapper.accountToAccountDto(account);
            sendRequest("lb://api-gateway/accounts/sync/deposit", accountDto);
            sendRequest("lb://api-gateway/transfer/sync/deposit", accountDto);
        } catch (Exception e) {
            log.error("Cannot sync deposit", e);
        }
    }

    private void sendRequest(String url, AccountDto body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var request = new HttpEntity<>(body, headers);
        log.info("Url {}", url);
        log.info("Body {}", body);
        // Send the request
        var response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Void.class
        );
    }
}

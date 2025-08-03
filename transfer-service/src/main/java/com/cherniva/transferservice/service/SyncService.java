package com.cherniva.transferservice.service;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.TransferDto;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {
    private final RestTemplate restTemplate;
    private final AccountMapper accountMapper;

    public void syncTransfer(Account sourceAccount, Account destinationAccount) {
        try {
            var sourceAccountDto = accountMapper.accountToAccountDto(sourceAccount);
            var destinationAccountDto = accountMapper.accountToAccountDto(destinationAccount);
            var transferDto = new TransferDto();
            transferDto.setSourceAccount(sourceAccountDto);
            transferDto.setDestinationAccount(destinationAccountDto);

            sendRequest("lb://api-gateway/accounts/sync/transfer", transferDto);
            sendRequest("lb://api-gateway/cash/sync/transfer", transferDto);
        } catch (Exception e) {
            log.error("Cannot sync withdraw", e);
        }
    }

    private void sendRequest(String url, TransferDto body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var request = new HttpEntity<>(body, headers);

        // Send the request
        var response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Void.class
        );
    }
}

package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.UserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {
    private final RestTemplate restTemplate;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;

    public void syncUserCreation(UserDetails userDetails) {
        try {
            var userAccountResponse = userMapper.userToUserAccountResponse(userDetails);

            sendUserCreationRequest("lb://api-gateway/cash/sync/createUser", userAccountResponse);
            sendUserCreationRequest("lb://api-gateway/transfer/sync/createUser", userAccountResponse);
        } catch (Exception e) {
            log.error("Cannot sync user creation", e);
        }
    }

    public void syncUserDeletion(Long userId) {
        try {
            sendDeletionRequest("lb://api-gateway/cash/sync/deleteUser", userId);
            sendDeletionRequest("lb://api-gateway/transfer/sync/createUser", userId);
        } catch (Exception e) {
            log.error("Cannot sync user creation", e);
        }
    }

    public void syncCreation(Account account) {
        try {
            var accountDto = accountMapper.accountToAccountDto(account);

            sendCreationRequest("lb://api-gateway/cash/sync/create", accountDto);
            sendCreationRequest("lb://api-gateway/transfer/sync/create", accountDto);
        } catch (Exception e) {
            log.error("Cannot sync account creation", e);
        }
    }

    public void syncDeletion(Long accountId) {
        try {
            sendDeletionRequest("lb://api-gateway/cash/sync/delete", accountId);
            sendDeletionRequest("lb://api-gateway/transfer/sync/delete", accountId);
        } catch (Exception e) {
            log.error("Cannot sync account deletion", e);
        }
    }

    private void sendUserCreationRequest(String url, UserAccountResponseDto body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("Sending request to: {}", url);
        log.info("Request body: {}", body);
        var request = new HttpEntity<>(body, headers);

        // Send the request
        var response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Void.class
        );
    }

    private void sendUserDeletionRequest(String url, Long userId) {
        var builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("userId", userId);

        var headers = new HttpHeaders();
        var request = new HttpEntity<>(headers);

        restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    private void sendCreationRequest(String url, AccountDto body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("Sending request to: {}", url);
        log.info("Request body: {}", body);
        var request = new HttpEntity<>(body, headers);

        // Send the request
        var response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Void.class
        );
    }

    private void sendDeletionRequest(String url, Long accountId) {
        var builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("accountId", accountId);

        var headers = new HttpHeaders();
        var request = new HttpEntity<>(headers);

        restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.PUT,
                request,
                Void.class
        );
    }
}

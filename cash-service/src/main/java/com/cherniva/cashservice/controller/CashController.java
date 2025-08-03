package com.cherniva.cashservice.controller;

import com.cherniva.cashservice.service.NotificationService;
import com.cherniva.cashservice.service.SessionService;
import com.cherniva.cashservice.service.SyncService;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.model.Account;
import com.cherniva.common.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {

//    private final AccountsService accountsService;
    private final AccountRepo accountRepo;
    private final SyncService syncService;
    private final SessionService sessionService;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    @PostMapping("/deposit")
    public ResponseEntity<UserAccountResponseDto> deposit(@RequestParam String sessionId, @RequestParam Long accountId, @RequestParam BigDecimal amount) {
        try {
            var valid = validOperation();
            if (valid) {
                Account account = accountRepo.findById(accountId).orElseThrow();
                account.setAmount(account.getAmount().add(amount));
                account = accountRepo.save(account);
                log.info("Successful deposit of {} {}. Current balance is {} {}", amount, account.getCurrency().getCode(), account.getAmount(), account.getCurrency().getCode());

                syncService.syncDeposit(account);

                var updatedUserAccountResponseDto = sessionService.updateSession(sessionId);

                notificationService.sendCashNotification(
                        updatedUserAccountResponseDto.getUserId().toString(),
                        updatedUserAccountResponseDto.getUsername(),
                        amount,
                        updatedUserAccountResponseDto.getAccounts().stream()
                                .filter(a -> a.getAccountId().equals(accountId))
                                .findFirst()
                                .map(a -> a.getCurrencyCode())
                                .orElse("UNKNOWN"),
                        accountId,
                        "deposit",
                        true // success
                );

                return ResponseEntity.ok(updatedUserAccountResponseDto);
            }
            return ResponseEntity.ofNullable(null);
        } catch (Exception e) {
            log.error("", e);
            notificationService.sendCashNotification(
                    null, // userId will be null for failed operations
                    null, // username will be null for failed operations
                    amount,
                    "UNKNOWN", // currency will be unknown for failed operations
                    accountId,
                    "deposit",
                    false // failed
            );
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<UserAccountResponseDto> withdraw(@RequestParam String sessionId, @RequestParam Long accountId, @RequestParam BigDecimal amount) {
        try {
            var valid = validOperation();
            if (valid) {
                Account account = accountRepo.findById(accountId).orElseThrow();
                if (amount.compareTo(account.getAmount()) > 0) {
                    throw new RuntimeException("amount greater than balance");
                }
                account.setAmount(account.getAmount().subtract(amount));
                account = accountRepo.save(account);

                log.info("Successful withdraw of {} {}. Current balance is {} {}", amount, account.getCurrency().getCode(), account.getAmount(), account.getCurrency().getCode());

                syncService.syncWithdraw(account);

                var updatedUserAccountResponseDto = sessionService.updateSession(sessionId);

                notificationService.sendCashNotification(
                        updatedUserAccountResponseDto.getUserId().toString(),
                        updatedUserAccountResponseDto.getUsername(),
                        amount,
                        updatedUserAccountResponseDto.getAccounts().stream()
                                .filter(a -> a.getAccountId().equals(accountId))
                                .findFirst()
                                .map(a -> a.getCurrencyCode())
                                .orElse("UNKNOWN"),
                        accountId,
                        "withdraw",
                        true // success
                );

                return ResponseEntity.ok(updatedUserAccountResponseDto);
            }
            return ResponseEntity.ofNullable(null);
        } catch (Exception e) {
            log.error("", e);
            notificationService.sendCashNotification(
                    null, // userId will be null for failed operations
                    null, // username will be null for failed operations
                    amount,
                    "UNKNOWN", // currency will be unknown for failed operations
                    accountId,
                    "withdraw",
                    false // failed
            );
            return ResponseEntity.badRequest().build();
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

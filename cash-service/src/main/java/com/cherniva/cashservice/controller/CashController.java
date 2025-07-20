package com.cherniva.cashservice.controller;

import com.cherniva.cashservice.service.AccountsService;
import com.cherniva.cashservice.service.NotificationService;
import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {

    private final AccountsService accountsService;
    private final NotificationService notificationService;

    @PostMapping("/deposit")
    public ResponseEntity<UserAccountResponseDto> deposit(@RequestParam String sessionId, @RequestParam Long accountId, @RequestParam BigDecimal amount) {
        try {
            var response = accountsService.deposit(sessionId, accountId, amount);

            // Send notification for successful deposit
            if (response != null) {
                notificationService.sendCashNotification(
                        response.getUserId().toString(),
                        response.getUsername(),
                        amount,
                        response.getAccounts().stream()
                                .filter(account -> account.getAccountId().equals(accountId))
                                .findFirst()
                                .map(account -> account.getCurrencyCode())
                                .orElse("UNKNOWN"),
                        accountId,
                        "deposit",
                        true // success
                );
            } else {
                // Send notification for failed operation (blocked by fraud detection)
                notificationService.sendCashNotification(
                        null, // userId will be null for failed operations
                        null, // username will be null for failed operations
                        amount,
                        "UNKNOWN", // currency will be unknown for failed operations
                        accountId,
                        "deposit",
                        false // failed
                );
            }

            return ResponseEntity.ofNullable(response);
        } catch (Exception e) {
            log.error("Deposit operation failed", e);
            
            // Send notification for failed operation (exception)
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
            var response = accountsService.withdraw(sessionId, accountId, amount);

            // Send notification for successful withdraw
            if (response != null) {
                notificationService.sendCashNotification(
                        response.getUserId().toString(),
                        response.getUsername(),
                        amount,
                        response.getAccounts().stream()
                                .filter(account -> account.getAccountId().equals(accountId))
                                .findFirst()
                                .map(account -> account.getCurrencyCode())
                                .orElse("UNKNOWN"),
                        accountId,
                        "withdraw",
                        true // success
                );
            } else {
                // Send notification for failed operation (blocked by fraud detection)
                notificationService.sendCashNotification(
                        null, // userId will be null for failed operations
                        null, // username will be null for failed operations
                        amount,
                        "UNKNOWN", // currency will be unknown for failed operations
                        accountId,
                        "withdraw",
                        false // failed
                );
            }

            return ResponseEntity.ofNullable(response);
        } catch (Exception e) {
            log.error("Withdraw operation failed", e);
            
            // Send notification for failed operation (exception)
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

}

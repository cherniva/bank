package com.cherniva.accountsservice.controller;

import com.cherniva.accountsservice.service.SessionService;
import com.cherniva.accountsservice.service.NotificationService;
import com.cherniva.accountsservice.service.SyncService;
import com.cherniva.accountsservice.utils.SeqGenerator;
import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.AccountRepo;
import com.cherniva.common.repo.CurrencyRepo;
import com.cherniva.common.repo.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountsController {
    private final UserMapper userMapper;
    private final CurrencyRepo currencyRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final SessionService sessionService;
    private final AccountRepo accountRepo;
    private final NotificationService notificationService;
    private final SyncService syncService;
    private final AccountMapper accountMapper;

    @PostMapping("/addAccount")
    public ResponseEntity<UserAccountResponseDto> addAccount(@RequestParam String sessionId, @RequestParam String currencyCode) {
        try {
            log.info("Creating account: {}, seesion {}", currencyCode, sessionId);
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();
            log.info("User: {}", userDetails);
            Account newAccount = new Account();
            newAccount.setId(SeqGenerator.getNextAccount());
            newAccount.setUserDetails(userDetails);
            newAccount.setAmount(BigDecimal.ZERO);
            newAccount.setCurrency(currencyRepo.findCurrencyByCode(currencyCode).orElseThrow());
            log.info("New account: {}", newAccount);
            userDetails.getAccounts().add(newAccount);

            UserDetails savedUser = userDetailsRepo.save(userDetails);
            log.info("Saved user: {}", savedUser);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(savedUser);
            sessionService.removeSession(sessionId);
            String newSessionId = sessionService.createSession(updatedUserAccountResponseDto);
            updatedUserAccountResponseDto.setSessionId(newSessionId);

            syncService.syncCreation(newAccount);
            
            // Send notification for successful account creation
            notificationService.sendAddAccountNotification(
                    userDetails.getId().toString(),
                    userDetails.getUsername(),
                    currencyCode,
                    true // success
            );
            
            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("Add account operation failed", e);
            
            // Send notification for failed operation (exception)
            try {
                SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
                if (sessionInfo != null && sessionInfo.getUserData() != null) {
                    notificationService.sendAddAccountNotification(
                            sessionInfo.getUserData().getUserId().toString(),
                            sessionInfo.getUserData().getUsername(),
                            currencyCode,
                            false // failed
                    );
                }
            } catch (Exception notificationException) {
                log.error("Failed to send add account failure notification", notificationException);
            }
            
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<UserAccountResponseDto> deleteAccount(@RequestParam String sessionId, @RequestParam Long accountId) {
        try {
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();
            
            // Find the account to delete
            Account accountToDelete = userDetails.getAccounts().stream()
                    .filter(account -> account.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Account not found or doesn't belong to user"));
            
            // Store currency code before deletion for notification
            String currencyCode = accountToDelete.getCurrency().getCode();
            
            // Validate that the account has zero balance
            if (accountToDelete.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                // Send notification for failed operation (non-zero balance)
                notificationService.sendDeleteAccountNotification(
                        userDetails.getId().toString(),
                        userDetails.getUsername(),
                        currencyCode,
                        false // failed
                );
                throw new RuntimeException("Cannot delete account with non-zero balance");
            }
            
            // Remove the account from user's accounts list
            userDetails.getAccounts().remove(accountToDelete);
            
            // Delete the account from database
            accountRepo.delete(accountToDelete);
            
            // Save updated user
            UserDetails savedUser = userDetailsRepo.save(userDetails);
            
            log.info("Account {} deleted successfully for user {}", accountId, userDetails.getUsername());
            
            // Update session with new user data
            sessionService.removeSession(sessionId);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(savedUser);
            String newSessionId = sessionService.createSession(updatedUserAccountResponseDto);
            updatedUserAccountResponseDto.setSessionId(newSessionId);

            syncService.syncDeletion(accountId);

            // Send notification for successful operation
            notificationService.sendDeleteAccountNotification(
                    userDetails.getId().toString(),
                    userDetails.getUsername(),
                    currencyCode,
                    true // success
            );
            
            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("Delete account operation failed", e);
            
            // Send notification for failed operation (exception)
            try {
                SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
                if (sessionInfo != null && sessionInfo.getUserData() != null) {
                    // Try to get currency code from the account if possible
                    String currencyCode = "Unknown";
                    try {
                        UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElse(null);
                        if (userDetails != null) {
                            Account accountToDelete = userDetails.getAccounts().stream()
                                    .filter(account -> account.getId().equals(accountId))
                                    .findFirst()
                                    .orElse(null);
                            if (accountToDelete != null) {
                                currencyCode = accountToDelete.getCurrency().getCode();
                            }
                        }
                    } catch (Exception currencyException) {
                        // Keep default "Unknown" currency code
                    }
                    
                    notificationService.sendDeleteAccountNotification(
                            sessionInfo.getUserData().getUserId().toString(),
                            sessionInfo.getUserData().getUsername(),
                            currencyCode,
                            false // failed
                    );
                }
            } catch (Exception notificationException) {
                log.error("Failed to send delete account failure notification", notificationException);
            }
            
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<AccountDto> getAccountByUsernameAndCurrency(@RequestParam String username, @RequestParam String currencyCode) {
        try {
            var user = userDetailsRepo.findByUsername(username).orElseThrow();
            var account = user.getAccounts().stream()
                    .filter(a -> a.getCurrency().getCode().equals(currencyCode))
                    .findFirst()
                    .orElseThrow();
            var accountDto = accountMapper.accountToAccountDto(account);

            return ResponseEntity.ok(accountDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

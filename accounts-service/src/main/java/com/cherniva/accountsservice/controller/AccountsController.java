package com.cherniva.accountsservice.controller;

import com.cherniva.accountsservice.service.SessionService;
import com.cherniva.accountsservice.service.NotificationService;
import com.cherniva.common.dto.ExchangeRateDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserRegistrationDto;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.AccountRepo;
import com.cherniva.common.repo.CurrencyRepo;
import com.cherniva.common.repo.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final AccountRepo accountRepo;
    private final NotificationService notificationService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello other service";
    }

    @PostMapping("/register")
    public ResponseEntity<UserAccountResponseDto> registerUser(@RequestBody UserRegistrationDto userRegistrationDto) {
        try {
            // Validate age - user must be at least 18 years old
            if (userRegistrationDto.getBirthdate() != null) {
                java.time.LocalDate eighteenYearsAgo = java.time.LocalDate.now().minusYears(18);
                if (userRegistrationDto.getBirthdate().isAfter(eighteenYearsAgo)) {
                    log.error("Registration attempt with birthdate that makes user younger than 18: {}", userRegistrationDto.getBirthdate());
                    return ResponseEntity.badRequest().build();
                }
            }
            
            UserDetails userDetails = userMapper.userRegistrationToUser(userRegistrationDto);
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            UserDetails savedUser = userDetailsRepo.save(userDetails);
            UserAccountResponseDto userAccountResponseDto = userMapper.userToUserAccountResponse(savedUser);
            return ResponseEntity.ok(userAccountResponseDto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/editPassword")
    public ResponseEntity<UserAccountResponseDto> editPassword(@RequestParam String sessionId, @RequestParam String password) {
        try {
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();
            userDetails.setPassword(passwordEncoder.encode(password));
            UserDetails savedUser = userDetailsRepo.save(userDetails);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(savedUser);
            sessionService.removeSession(sessionId);
            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/editUser")
    public ResponseEntity<UserAccountResponseDto> editUser(@RequestParam String sessionId, 
                                                          @RequestParam(required = false) String name,
                                                          @RequestParam(required = false) String surname,
                                                          @RequestParam(required = false) String birthdate) {
        try {
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();
            
            // Track what fields are being updated
            boolean nameUpdated = false;
            boolean surnameUpdated = false;
            boolean birthdateUpdated = false;
            
            // Update name if provided
            if (name != null && !name.trim().isEmpty()) {
                userDetails.setName(name.trim());
                nameUpdated = true;
            }
            
            // Update surname if provided
            if (surname != null && !surname.trim().isEmpty()) {
                userDetails.setSurname(surname.trim());
                surnameUpdated = true;
            }
            
            // Update birthdate if provided
            if (birthdate != null && !birthdate.trim().isEmpty()) {
                try {
                    java.time.LocalDate birthDate = java.time.LocalDate.parse(birthdate);
                    java.time.LocalDate eighteenYearsAgo = java.time.LocalDate.now().minusYears(18);
                    
                    if (birthDate.isAfter(eighteenYearsAgo)) {
                        log.error("User attempted to set birthdate that makes them younger than 18: {}", birthdate);
                        
                        // Send notification for failed operation
                        notificationService.sendEditUserNotification(
                                userDetails.getId().toString(),
                                userDetails.getUsername(),
                                nameUpdated ? name : null,
                                surnameUpdated ? surname : null,
                                birthdate,
                                false // failed
                        );
                        
                        return ResponseEntity.badRequest().build();
                    }
                    
                    userDetails.setBirthdate(birthDate);
                    birthdateUpdated = true;
                } catch (Exception e) {
                    log.error("Invalid birthdate format: {}", birthdate, e);
                    
                    // Send notification for failed operation
                    notificationService.sendEditUserNotification(
                            userDetails.getId().toString(),
                            userDetails.getUsername(),
                            nameUpdated ? name : null,
                            surnameUpdated ? surname : null,
                            birthdate,
                            false // failed
                    );
                    
                    return ResponseEntity.badRequest().build();
                }
            }
            
            UserDetails savedUser = userDetailsRepo.save(userDetails);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(savedUser);
            
            // Remove old session and create new one
            sessionService.removeSession(sessionId);
            String newSessionId = sessionService.createSession(updatedUserAccountResponseDto);
            updatedUserAccountResponseDto.setSessionId(newSessionId);
            
            // Send notification for successful operation
            notificationService.sendEditUserNotification(
                    userDetails.getId().toString(),
                    userDetails.getUsername(),
                    nameUpdated ? name : null,
                    surnameUpdated ? surname : null,
                    birthdateUpdated ? birthdate : null,
                    true // success
            );
            
            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("Edit user operation failed", e);
            
            // Send notification for failed operation (exception)
            try {
                SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
                if (sessionInfo != null && sessionInfo.getUserData() != null) {
                    notificationService.sendEditUserNotification(
                            sessionInfo.getUserData().getUserId().toString(),
                            sessionInfo.getUserData().getUsername(),
                            name,
                            surname,
                            birthdate,
                            false // failed
                    );
                }
            } catch (Exception notificationException) {
                log.error("Failed to send edit user failure notification", notificationException);
            }
            
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam String sessionId) {
        try {
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();
            userDetailsRepo.delete(userDetails);
            sessionService.removeSession(sessionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/addAccount")
    public ResponseEntity<UserAccountResponseDto> addAccount(@RequestParam String sessionId, @RequestParam String currencyCode) {
        try {
            log.info("Creating account: {}, seesion {}", currencyCode, sessionId);
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();
            log.info("User: {}", userDetails);
            Account newAccount = new Account();
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

    @PostMapping("/deposit")
    public ResponseEntity<UserAccountResponseDto> deposit(@RequestParam String sessionId, @RequestParam Long accountId, @RequestParam BigDecimal amount) {
        try {
            Account account = accountRepo.findById(accountId).orElseThrow();
            account.setAmount(account.getAmount().add(amount));
            account = accountRepo.save(account);
            log.info("Successful deposit of {} {}. Current balance is {} {}", amount, account.getCurrency().getCode(), account.getAmount(), account.getCurrency().getCode());

            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserAccountResponseDto userAccountResponseDto = sessionInfo.getUserData();
            UserDetails userDetails = userDetailsRepo.findById(userAccountResponseDto.getUserId()).orElseThrow();
            sessionService.removeSession(sessionId);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(userDetails);
            String newSessionId = sessionService.createSession(updatedUserAccountResponseDto);
            updatedUserAccountResponseDto.setSessionId(newSessionId);

            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<UserAccountResponseDto> withdraw(@RequestParam String sessionId, @RequestParam Long accountId, @RequestParam BigDecimal amount) {
        try {
            Account account = accountRepo.findById(accountId).orElseThrow();
            if (amount.compareTo(account.getAmount()) > 0) {
                throw new RuntimeException("amount greater than balance");
            }
            account.setAmount(account.getAmount().subtract(amount));
            account = accountRepo.save(account);

            log.info("Successful withdraw of {} {}. Current balance is {} {}", amount, account.getCurrency().getCode(), account.getAmount(), account.getCurrency().getCode());

            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserAccountResponseDto userAccountResponseDto = sessionInfo.getUserData();
            UserDetails userDetails = userDetailsRepo.findById(userAccountResponseDto.getUserId()).orElseThrow();
            sessionService.removeSession(sessionId);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(userDetails);
            String newSessionId = sessionService.createSession(updatedUserAccountResponseDto);
            updatedUserAccountResponseDto.setSessionId(newSessionId);

            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/transfer")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<UserAccountResponseDto> transfer(@RequestParam String sessionId, @RequestParam BigDecimal amount,
                                                           @RequestParam String username, @RequestBody ExchangeRateDto exchangeRateDto) {
        try {
            // Get session info and user details
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserAccountResponseDto userAccountResponseDto = sessionInfo.getUserData();
            UserDetails sourceUser = userDetailsRepo.findById(userAccountResponseDto.getUserId()).orElseThrow();
            
            // Find source account with matching currency
            Account sourceAccount = sourceUser.getAccounts().stream()
                    .filter(account -> account.getCurrency().getCode().equals(exchangeRateDto.getFromCurrency()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Source account with currency " + exchangeRateDto.getFromCurrency() + " not found"));
            
            // Validate sufficient balance
            if (amount.compareTo(sourceAccount.getAmount()) > 0) {
                throw new RuntimeException("Insufficient balance for transfer");
            }
            
            // Find target user by username
            UserDetails targetUser = userDetailsRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Target user not found"));
            
            // Find target account with matching currency
            Account targetAccount = targetUser.getAccounts().stream()
                    .filter(account -> account.getCurrency().getCode().equals(exchangeRateDto.getToCurrency()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Target account with currency " + exchangeRateDto.getToCurrency() + " not found"));
            
            // Calculate converted amount using exchange rate
            BigDecimal convertedAmount = amount.multiply(exchangeRateDto.getSellRate());
            
            // Perform the transfer
            sourceAccount.setAmount(sourceAccount.getAmount().subtract(amount));
            targetAccount.setAmount(targetAccount.getAmount().add(convertedAmount));
            
            // Save both accounts
            accountRepo.save(sourceAccount);
            accountRepo.save(targetAccount);
            
            log.info("Successful transfer of {} {} to {} {} (converted amount: {} {})", 
                    amount, exchangeRateDto.getFromCurrency(), 
                    username, exchangeRateDto.getToCurrency(),
                    convertedAmount, exchangeRateDto.getToCurrency());
            
            // Update session with new user data
            sessionService.removeSession(sessionId);
            UserDetails updatedSourceUser = userDetailsRepo.findById(sourceUser.getId()).orElseThrow();
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(updatedSourceUser);
            String newSessionId = sessionService.createSession(updatedUserAccountResponseDto);
            updatedUserAccountResponseDto.setSessionId(newSessionId);
            
            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }
}

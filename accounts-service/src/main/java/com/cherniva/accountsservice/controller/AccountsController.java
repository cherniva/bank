package com.cherniva.accountsservice.controller;

import com.cherniva.accountsservice.service.SessionService;
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

    @GetMapping("/hello")
    public String hello() {
        return "Hello other service";
    }

    @PostMapping("/register")
    public ResponseEntity<UserAccountResponseDto> registerUser(@RequestBody UserRegistrationDto userRegistrationDto) {
        try {
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
            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("", e);
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
}

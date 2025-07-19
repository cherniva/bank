package com.cherniva.cashservice.controller;

import com.cherniva.cashservice.service.AccountsService;
import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {

    private final AccountsService accountsService;

    @PostMapping("/deposit")
    public ResponseEntity<UserAccountResponseDto> deposit(@RequestParam String sessionId, @RequestParam Long accountId, @RequestParam BigDecimal amount) {
        try {
            var response = accountsService.deposit(sessionId, accountId, amount);

            return ResponseEntity.ofNullable(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<UserAccountResponseDto> withdraw(@RequestParam String sessionId, @RequestParam Long accountId, @RequestParam BigDecimal amount) {
        try {
            var response = accountsService.withdraw(sessionId, accountId, amount);

            return ResponseEntity.ofNullable(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}

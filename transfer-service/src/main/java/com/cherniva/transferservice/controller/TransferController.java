package com.cherniva.transferservice.controller;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<UserAccountResponseDto> transfer(@RequestParam String sessionId, @RequestParam BigDecimal amount,
                                                           @RequestParam String fromCurrency, @RequestParam String toCurrency,
                                                           @RequestParam String username) {
        try {
            var response = transferService.transfer(sessionId, amount, fromCurrency, toCurrency, username);

            return ResponseEntity.ofNullable(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

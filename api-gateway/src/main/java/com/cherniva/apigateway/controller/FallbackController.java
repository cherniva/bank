package com.cherniva.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/accounts")
    @PostMapping("/accounts")
    public ResponseEntity<Map<String, Object>> accountsFallback() {
        return createFallbackResponse("Accounts service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/blocker")
    @PostMapping("/blocker")
    public ResponseEntity<Map<String, Object>> blockerFallback() {
        return createFallbackResponse("Blocker service is temporarily unavailable. Request will be processed without blocking check.");
    }

    @GetMapping("/cash")
    @PostMapping("/cash")
    public ResponseEntity<Map<String, Object>> cashFallback() {
        return createFallbackResponse("Cash service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/exchange")
    @PostMapping("/exchange")
    public ResponseEntity<Map<String, Object>> exchangeFallback() {
        return createFallbackResponse("Exchange rate service is temporarily unavailable. Using cached rates if available.");
    }

    @GetMapping("/notifications")
    @PostMapping("/notifications")
    public ResponseEntity<Map<String, Object>> notificationsFallback() {
        return createFallbackResponse("Notifications service is temporarily unavailable. Notification will be queued for later delivery.");
    }

    @GetMapping("/transfer")
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transferFallback() {
        return createFallbackResponse("Transfer service is temporarily unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> response = Map.of(
            "status", "SERVICE_UNAVAILABLE",
            "message", message,
            "timestamp", LocalDateTime.now(),
            "fallback", true
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
} 
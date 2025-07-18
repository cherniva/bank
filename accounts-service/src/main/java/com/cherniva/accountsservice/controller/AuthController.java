package com.cherniva.accountsservice.controller;

import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.SessionValidationDto;
import com.cherniva.accountsservice.service.AuthService;
import com.cherniva.accountsservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    @PostMapping("/login")
    public ResponseEntity<UserAccountResponseDto> login(@RequestBody UserLoginDto loginDto) {
        System.out.println("Here");
        try {
            UserAccountResponseDto response = authService.authenticateUser(loginDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/validate-session")
    public ResponseEntity<SessionValidationDto> validateSession(@RequestParam String sessionId) {
        SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
        
        SessionValidationDto response = new SessionValidationDto();
        response.setSessionId(sessionId);
        
        if (sessionInfo != null) {
            response.setValid(true);
            response.setUsername(sessionInfo.getUsername());
            response.setUserId(sessionInfo.getUserId());
        } else {
            response.setValid(false);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String sessionId) {
        sessionService.removeSession(sessionId);
        return ResponseEntity.ok().build();
    }
} 
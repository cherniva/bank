package com.cherniva.accountsservice.controller;

import com.cherniva.accountsservice.service.SessionService;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserRegistrationDto;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountsController {
    private final UserMapper userMapper;
    private final UserDetailsRepo userDetailsRepo;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

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
}

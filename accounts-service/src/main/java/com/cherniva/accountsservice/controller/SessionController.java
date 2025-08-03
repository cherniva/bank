package com.cherniva.accountsservice.controller;

import com.cherniva.accountsservice.service.SessionService;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts/session")
@RequiredArgsConstructor
@Slf4j
public class SessionController {
    private final UserMapper userMapper;
    private final SessionService sessionService;
    private final UserDetailsRepo userDetailsRepo;

    @PutMapping("/update")
    public ResponseEntity<UserAccountResponseDto> updateSession(@RequestParam String sessionId) {
        try {
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserAccountResponseDto userAccountResponseDto = sessionInfo.getUserData();
            UserDetails userDetails = userDetailsRepo.findById(userAccountResponseDto.getUserId()).orElseThrow();
            sessionService.removeSession(sessionId);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(userDetails);
            String newSessionId = sessionService.createSession(updatedUserAccountResponseDto);
            updatedUserAccountResponseDto.setSessionId(newSessionId);

            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

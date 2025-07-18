package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.UserDetailsRepo;
import com.cherniva.common.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserDetailsRepo userDetailsRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SessionService sessionService;

    public UserAccountResponseDto authenticateUser(UserLoginDto userLoginDto) {
        Optional<UserDetails> userDetailsOpt = userDetailsRepo.findByUsername(userLoginDto.getUsername());
        
        if (userDetailsOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        UserDetails userDetails = userDetailsOpt.get();
        
        if (!passwordEncoder.matches(userLoginDto.getPassword(), userDetails.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        // Create UserAccountResponseDto first
        UserAccountResponseDto response = userMapper.userToUserAccountResponse(userDetails);
        
        // Create session with complete user data
        String sessionId = sessionService.createSession(response);
        response.setSessionId(sessionId);
        
        return response;
    }
}

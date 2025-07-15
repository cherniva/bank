package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.UserDetailsRepo;
import com.cherniva.common.mapper.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserDetailsRepo userDetailsRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountMapper userAccountMapper;
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
        
        // Create session
        String sessionId = sessionService.createSession(userDetails.getUsername(), userDetails.getId());
        
        UserAccountResponseDto response = userAccountMapper.userToUserAccountResponse(userDetails);
        response.setSessionId(sessionId); // You'll need to add this field to UserAccountResponseDto
        
        return response;
    }
}

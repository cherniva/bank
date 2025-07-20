package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.UserDetailsRepo;
import com.cherniva.common.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsRepo userDetailsRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private AuthService authService;

    private UserDetails testUser;
    private UserLoginDto loginDto;
    private UserAccountResponseDto expectedResponse;

    @BeforeEach
    void setUp() {
        testUser = new UserDetails();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setBirthdate(LocalDate.of(1990, 1, 1));

        loginDto = new UserLoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("plainPassword");

        expectedResponse = new UserAccountResponseDto();
        expectedResponse.setUserId(1L);
        expectedResponse.setUsername("testuser");
        expectedResponse.setName("John");
        expectedResponse.setSurname("Doe");
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsUserAccountResponse() {
        // Arrange
        when(userDetailsRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);
        when(userMapper.userToUserAccountResponse(testUser)).thenReturn(expectedResponse);
        when(sessionService.createSession(any(UserAccountResponseDto.class))).thenReturn("session123");

        // Act
        UserAccountResponseDto result = authService.authenticateUser(loginDto);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("session123", result.getSessionId());
        verify(userDetailsRepo).findByUsername("testuser");
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
        verify(userMapper).userToUserAccountResponse(testUser);
        verify(sessionService).createSession(any(UserAccountResponseDto.class));
    }

    @Test
    void authenticateUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userDetailsRepo.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.authenticateUser(loginDto));
        assertEquals("User not found", exception.getMessage());
        verify(userDetailsRepo).findByUsername("testuser");
        verifyNoInteractions(passwordEncoder, userMapper, sessionService);
    }

    @Test
    void authenticateUser_InvalidPassword_ThrowsException() {
        // Arrange
        when(userDetailsRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.authenticateUser(loginDto));
        assertEquals("Invalid password", exception.getMessage());
        verify(userDetailsRepo).findByUsername("testuser");
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
        verifyNoInteractions(userMapper, sessionService);
    }
} 
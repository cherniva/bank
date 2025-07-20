package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    private UserLoginDto loginDto;
    private UserAccountResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        loginDto = new UserLoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("password");

        mockResponse = new UserAccountResponseDto();
        mockResponse.setUserId(1L);
        mockResponse.setUsername("testuser");
        mockResponse.setSessionId("session123");
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsUserAccountResponse() {
        // Arrange
        when(restTemplate.postForObject(anyString(), eq(loginDto), eq(UserAccountResponseDto.class)))
                .thenReturn(mockResponse);

        // Act
        UserAccountResponseDto result = authService.authenticateUser(loginDto);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("session123", result.getSessionId());
        verify(restTemplate).postForObject("lb://api-gateway/api/auth/login", loginDto, UserAccountResponseDto.class);
    }

    @Test
    void authenticateUser_ServiceException_ThrowsRuntimeException() {
        // Arrange
        when(restTemplate.postForObject(anyString(), eq(loginDto), eq(UserAccountResponseDto.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticateUser(loginDto));
        assertEquals("Authentication failed", exception.getMessage());
        verify(restTemplate).postForObject("lb://api-gateway/api/auth/login", loginDto, UserAccountResponseDto.class);
    }

    @Test
    void authenticateUser_NullResponse_ThrowsRuntimeException() {
        // Arrange
        when(restTemplate.postForObject(anyString(), eq(loginDto), eq(UserAccountResponseDto.class)))
                .thenReturn(null);

        // Act
        UserAccountResponseDto result = authService.authenticateUser(loginDto);

        // Assert
        assertNull(result);
        verify(restTemplate).postForObject("lb://api-gateway/api/auth/login", loginDto, UserAccountResponseDto.class);
    }

    @Test
    void authenticateUser_InvalidCredentials_ThrowsRuntimeException() {
        // Arrange
        when(restTemplate.postForObject(anyString(), eq(loginDto), eq(UserAccountResponseDto.class)))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticateUser(loginDto));
        assertEquals("Authentication failed", exception.getMessage());
    }
} 
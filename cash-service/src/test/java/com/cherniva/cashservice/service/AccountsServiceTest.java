package com.cherniva.cashservice.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AccountsService accountsService;

    private UserAccountResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new UserAccountResponseDto();
        mockResponse.setUserId(1L);
        mockResponse.setUsername("testuser");
    }

    @Test
    void deposit_ValidOperation_ReturnsUserAccountResponse() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(UserAccountResponseDto.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        // Act
        UserAccountResponseDto result = accountsService.deposit("session123", 1L, BigDecimal.valueOf(100));

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(restTemplate, times(2)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void deposit_BlockedOperation_ReturnsNull() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        // Act
        UserAccountResponseDto result = accountsService.deposit("session123", 1L, BigDecimal.valueOf(100));

        // Assert
        assertNull(result);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class));
    }

    @Test
    void deposit_ExceptionThrown_ThrowsRuntimeException() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            accountsService.deposit("session123", 1L, BigDecimal.valueOf(100)));
    }

    @Test
    void withdraw_ValidOperation_ReturnsUserAccountResponse() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(UserAccountResponseDto.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        // Act
        UserAccountResponseDto result = accountsService.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(restTemplate, times(2)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void withdraw_BlockedOperation_ReturnsNull() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        // Act
        UserAccountResponseDto result = accountsService.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertNull(result);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class));
    }

    @Test
    void withdraw_ExceptionThrown_ThrowsRuntimeException() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            accountsService.withdraw("session123", 1L, BigDecimal.valueOf(50)));
    }
} 
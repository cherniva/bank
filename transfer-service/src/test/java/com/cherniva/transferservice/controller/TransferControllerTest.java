package com.cherniva.transferservice.controller;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.transferservice.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    private UserAccountResponseDto mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUserResponse = new UserAccountResponseDto();
        mockUserResponse.setUserId(1L);
        mockUserResponse.setUsername("testuser");
    }

    @Test
    void transfer_SuccessfulOperation_ReturnsOkResponse() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        when(transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username))
                .thenReturn(mockUserResponse);

        // Act
        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
                sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(transferService).transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);
    }

    @Test
    void transfer_BlockedOperation_ReturnsNotFoundResponse() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        when(transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username))
                .thenReturn(null);

        // Act
        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
                sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(transferService).transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);
    }

    @Test
    void transfer_ExceptionThrown_ReturnsBadRequest() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        when(transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
                sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        verify(transferService).transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);
    }

    @Test
    void transfer_WithNullParameters_HandlesGracefully() {
        // Arrange
        when(transferService.transfer(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Invalid parameters"));

        // Act
        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
                null, null, null, null, null, null);

        // Assert
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void transfer_InsufficientBalance_ReturnsBadRequest() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("1000.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        when(transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username))
                .thenThrow(new RuntimeException("Insufficient balance for transfer"));

        // Act
        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
                sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(transferService).transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);
    }

    @Test
    void transfer_AccountNotFound_ReturnsBadRequest() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 999L; // Non-existent account
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        when(transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username))
                .thenThrow(new RuntimeException("Source account not found"));

        // Act
        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
                sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(transferService).transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);
    }
} 
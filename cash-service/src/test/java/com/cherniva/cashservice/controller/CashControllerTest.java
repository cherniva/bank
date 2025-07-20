package com.cherniva.cashservice.controller;

import com.cherniva.cashservice.service.AccountsService;
import com.cherniva.cashservice.service.NotificationService;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.AccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashControllerTest {

    @Mock
    private AccountsService accountsService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CashController cashController;

    private UserAccountResponseDto mockUserResponse;
    private AccountDto mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = new AccountDto();
        mockAccount.setAccountId(1L);
        mockAccount.setCurrencyCode("USD");
        mockAccount.setCurrencyName("US Dollar");
        mockAccount.setAmount(BigDecimal.valueOf(1000));

        mockUserResponse = new UserAccountResponseDto();
        mockUserResponse.setUserId(1L);
        mockUserResponse.setUsername("testuser");
        mockUserResponse.setAccounts(List.of(mockAccount));
    }

    @Test
    void deposit_SuccessfulOperation_ReturnsOkResponse() {
        // Arrange
        when(accountsService.deposit("session123", 1L, BigDecimal.valueOf(100)))
                .thenReturn(mockUserResponse);

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.deposit("session123", 1L, BigDecimal.valueOf(100));

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(accountsService).deposit("session123", 1L, BigDecimal.valueOf(100));
        verify(notificationService).sendCashNotification(
                eq("1"), eq("testuser"), eq(BigDecimal.valueOf(100)), 
                eq("USD"), eq(1L), eq("deposit"), eq(true));
    }

    @Test
    void deposit_BlockedOperation_ReturnsNullResponse() {
        // Arrange
        when(accountsService.deposit("session123", 1L, BigDecimal.valueOf(100)))
                .thenReturn(null);

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.deposit("session123", 1L, BigDecimal.valueOf(100));

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(accountsService).deposit("session123", 1L, BigDecimal.valueOf(100));
        verify(notificationService).sendCashNotification(
                isNull(), isNull(), eq(BigDecimal.valueOf(100)), 
                eq("UNKNOWN"), eq(1L), eq("deposit"), eq(false));
    }

    @Test
    void deposit_ExceptionThrown_ReturnsBadRequestWithNotification() {
        // Arrange
        when(accountsService.deposit("session123", 1L, BigDecimal.valueOf(100)))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.deposit("session123", 1L, BigDecimal.valueOf(100));

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(notificationService).sendCashNotification(
                isNull(), isNull(), eq(BigDecimal.valueOf(100)), 
                eq("UNKNOWN"), eq(1L), eq("deposit"), eq(false));
    }

    @Test
    void withdraw_SuccessfulOperation_ReturnsOkResponse() {
        // Arrange
        when(accountsService.withdraw("session123", 1L, BigDecimal.valueOf(50)))
                .thenReturn(mockUserResponse);

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(accountsService).withdraw("session123", 1L, BigDecimal.valueOf(50));
        verify(notificationService).sendCashNotification(
                eq("1"), eq("testuser"), eq(BigDecimal.valueOf(50)), 
                eq("USD"), eq(1L), eq("withdraw"), eq(true));
    }

    @Test
    void withdraw_BlockedOperation_ReturnsNullResponse() {
        // Arrange
        when(accountsService.withdraw("session123", 1L, BigDecimal.valueOf(50)))
                .thenReturn(null);

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(accountsService).withdraw("session123", 1L, BigDecimal.valueOf(50));
        verify(notificationService).sendCashNotification(
                isNull(), isNull(), eq(BigDecimal.valueOf(50)), 
                eq("UNKNOWN"), eq(1L), eq("withdraw"), eq(false));
    }

    @Test
    void withdraw_ExceptionThrown_ReturnsBadRequestWithNotification() {
        // Arrange
        when(accountsService.withdraw("session123", 1L, BigDecimal.valueOf(50)))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(notificationService).sendCashNotification(
                isNull(), isNull(), eq(BigDecimal.valueOf(50)), 
                eq("UNKNOWN"), eq(1L), eq("withdraw"), eq(false));
    }
} 
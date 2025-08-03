package com.cherniva.cashservice.controller;

import com.cherniva.cashservice.service.NotificationService;
import com.cherniva.cashservice.service.SessionService;
import com.cherniva.cashservice.service.SyncService;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.repo.AccountRepo;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashControllerTest {

    @Mock
    private AccountRepo accountRepo;

    @Mock
    private SyncService syncService;

    @Mock
    private SessionService sessionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CashController cashController;

    private UserAccountResponseDto mockUserResponse;
    private AccountDto mockAccountDto;
    private Account mockAccount;
    private Currency mockCurrency;

    @BeforeEach
    void setUp() {
        mockCurrency = new Currency();
        mockCurrency.setCode("USD");
        mockCurrency.setName("US Dollar");

        mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setCurrency(mockCurrency);
        mockAccount.setAmount(BigDecimal.valueOf(1000));

        mockAccountDto = new AccountDto();
        mockAccountDto.setAccountId(1L);
        mockAccountDto.setCurrencyCode("USD");
        mockAccountDto.setCurrencyName("US Dollar");
        mockAccountDto.setAmount(BigDecimal.valueOf(1100)); // Updated amount after deposit

        mockUserResponse = new UserAccountResponseDto();
        mockUserResponse.setUserId(1L);
        mockUserResponse.setUsername("testuser");
        mockUserResponse.setAccounts(List.of(mockAccountDto));
    }

    @Test
    void deposit_SuccessfulOperation_ReturnsOkResponse() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(mockAccount));
        when(accountRepo.save(any(Account.class))).thenReturn(mockAccount);
        when(sessionService.updateSession("session123")).thenReturn(mockUserResponse);

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.deposit("session123", 1L, BigDecimal.valueOf(100));

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(accountRepo).findById(1L);
        verify(accountRepo).save(any(Account.class));
        verify(syncService).syncDeposit(any(Account.class));
        verify(sessionService).updateSession("session123");
        verify(notificationService).sendCashNotification(
                eq("1"), eq("testuser"), eq(BigDecimal.valueOf(100)), 
                eq("USD"), eq(1L), eq("deposit"), eq(true));
    }

    @Test
    void deposit_BlockedOperation_ReturnsNullResponse() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.deposit("session123", 1L, BigDecimal.valueOf(100));

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(accountRepo, never()).findById(anyLong());
        verify(syncService, never()).syncDeposit(any());
        verify(sessionService, never()).updateSession(anyString());
        verify(notificationService, never()).sendCashNotification(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void deposit_ExceptionThrown_ReturnsBadRequestWithNotification() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));
        when(accountRepo.findById(1L)).thenThrow(new RuntimeException("Database error"));

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
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(mockAccount));
        when(accountRepo.save(any(Account.class))).thenReturn(mockAccount);
        when(sessionService.updateSession("session123")).thenReturn(mockUserResponse);

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(accountRepo).findById(1L);
        verify(accountRepo).save(any(Account.class));
        verify(syncService).syncWithdraw(any(Account.class));
        verify(sessionService).updateSession("session123");
        verify(notificationService).sendCashNotification(
                eq("1"), eq("testuser"), eq(BigDecimal.valueOf(50)), 
                eq("USD"), eq(1L), eq("withdraw"), eq(true));
    }

    @Test
    void withdraw_InsufficientFunds_ReturnsBadRequestWithNotification() {
        // Arrange
        mockAccount.setAmount(BigDecimal.valueOf(10)); // Less than withdrawal amount
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(mockAccount));

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(notificationService).sendCashNotification(
                isNull(), isNull(), eq(BigDecimal.valueOf(50)), 
                eq("UNKNOWN"), eq(1L), eq("withdraw"), eq(false));
    }

    @Test
    void withdraw_BlockedOperation_ReturnsNullResponse() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(accountRepo, never()).findById(anyLong());
        verify(syncService, never()).syncWithdraw(any());
        verify(sessionService, never()).updateSession(anyString());
        verify(notificationService, never()).sendCashNotification(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void withdraw_ExceptionThrown_ReturnsBadRequestWithNotification() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));
        when(accountRepo.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<UserAccountResponseDto> response = cashController.withdraw("session123", 1L, BigDecimal.valueOf(50));

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(notificationService).sendCashNotification(
                isNull(), isNull(), eq(BigDecimal.valueOf(50)), 
                eq("UNKNOWN"), eq(1L), eq("withdraw"), eq(false));
    }
} 
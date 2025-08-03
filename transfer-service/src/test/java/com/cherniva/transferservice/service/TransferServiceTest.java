package com.cherniva.transferservice.service;

import com.cherniva.common.dto.ExchangeRateDto;
import com.cherniva.common.dto.ExchangeRatesResponseDto;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

//    @Mock
//    private RestTemplate restTemplate;
//
//    @Mock
//    private NotificationService notificationService;
//
//    @InjectMocks
//    private TransferService transferService;
//
//    private UserAccountResponseDto mockUserResponse;
//    private ExchangeRatesResponseDto mockExchangeRates;
//    private ExchangeRateDto mockExchangeRate;
//
//    @BeforeEach
//    void setUp() {
//        mockUserResponse = new UserAccountResponseDto();
//        mockUserResponse.setUserId(1L);
//        mockUserResponse.setUsername("testuser");
//
//        mockExchangeRate = new ExchangeRateDto();
//        mockExchangeRate.setFromCurrency("USD");
//        mockExchangeRate.setToCurrency("EUR");
//        mockExchangeRate.setBuyRate(BigDecimal.valueOf(0.85));
//        mockExchangeRate.setSellRate(BigDecimal.valueOf(0.83));
//        mockExchangeRate.setLastUpdated(LocalDateTime.now());
//
//        mockExchangeRates = new ExchangeRatesResponseDto();
//        mockExchangeRates.setRates(List.of(mockExchangeRate));
//        mockExchangeRates.setSuccess(true);
//    }
//
//    @Test
//    void transfer_ValidOperation_ReturnsUserAccountResponse() {
//        // Arrange
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
//                .thenReturn(ResponseEntity.ok(true));
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ExchangeRatesResponseDto.class)))
//                .thenReturn(ResponseEntity.ok(mockExchangeRates));
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(UserAccountResponseDto.class)))
//                .thenReturn(ResponseEntity.ok(mockUserResponse));
//
//        // Act
//        UserAccountResponseDto result = transferService.transfer("session123", BigDecimal.valueOf(100),
//                "USD", "EUR", "recipient");
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("testuser", result.getUsername());
//        verify(notificationService).sendTransferNotification(
//                eq("1"), eq("testuser"), eq(BigDecimal.valueOf(100)),
//                eq("USD"), eq("EUR"), eq("recipient"), any(BigDecimal.class), eq(true));
//    }
//
//    @Test
//    void transfer_SameCurrency_UsesOneToOneRate() {
//        // Arrange
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
//                .thenReturn(ResponseEntity.ok(true));
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(UserAccountResponseDto.class)))
//                .thenReturn(ResponseEntity.ok(mockUserResponse));
//
//        // Act
//        UserAccountResponseDto result = transferService.transfer("session123", BigDecimal.valueOf(100),
//                "USD", "USD", "recipient");
//
//        // Assert
//        assertNotNull(result);
//        verify(notificationService).sendTransferNotification(
//                eq("1"), eq("testuser"), eq(BigDecimal.valueOf(100)),
//                eq("USD"), eq("USD"), eq("recipient"), eq(BigDecimal.valueOf(100)), eq(true));
//    }
//
//    @Test
//    void transfer_BlockedOperation_ReturnsNull() {
//        // Arrange
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
//                .thenReturn(ResponseEntity.ok(false));
//
//        // Act
//        UserAccountResponseDto result = transferService.transfer("session123", BigDecimal.valueOf(100),
//                "USD", "EUR", "recipient");
//
//        // Assert
//        assertNull(result);
//        verify(notificationService).sendTransferNotification(
//                isNull(), isNull(), eq(BigDecimal.valueOf(100)),
//                eq("USD"), eq("EUR"), eq("recipient"), isNull(), eq(false));
//    }
//
//    @Test
//    void transfer_ExceptionThrown_ThrowsRuntimeException() {
//        // Arrange
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
//                .thenThrow(new RuntimeException("Network error"));
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () ->
//            transferService.transfer("session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient"));
//
//        verify(notificationService).sendTransferNotification(
//                isNull(), isNull(), eq(BigDecimal.valueOf(100)),
//                eq("USD"), eq("EUR"), eq("recipient"), isNull(), eq(false));
//    }
//
//    @Test
//    void transfer_ReverseExchangeRate_CalculatesCorrectly() {
//        // Arrange
//        ExchangeRateDto reverseRate = new ExchangeRateDto();
//        reverseRate.setFromCurrency("EUR");
//        reverseRate.setToCurrency("USD");
//        reverseRate.setBuyRate(BigDecimal.valueOf(1.18));
//        reverseRate.setSellRate(BigDecimal.valueOf(1.20));
//
//        ExchangeRatesResponseDto reverseRates = new ExchangeRatesResponseDto();
//        reverseRates.setRates(List.of(reverseRate));
//        reverseRates.setSuccess(true);
//
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Boolean.class)))
//                .thenReturn(ResponseEntity.ok(true));
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ExchangeRatesResponseDto.class)))
//                .thenReturn(ResponseEntity.ok(reverseRates));
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(UserAccountResponseDto.class)))
//                .thenReturn(ResponseEntity.ok(mockUserResponse));
//
//        // Act
//        UserAccountResponseDto result = transferService.transfer("session123", BigDecimal.valueOf(100),
//                "USD", "EUR", "recipient");
//
//        // Assert
//        assertNotNull(result);
//        verify(notificationService).sendTransferNotification(
//                eq("1"), eq("testuser"), eq(BigDecimal.valueOf(100)),
//                eq("USD"), eq("EUR"), eq("recipient"), any(BigDecimal.class), eq(true));
//    }
} 
package com.cherniva.transferservice.service;

import com.cherniva.common.dto.ExchangeRateDto;
import com.cherniva.common.dto.ExchangeRatesResponseDto;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AccountRepo accountRepo;

    @Mock
    private AccountService accountService;

    @Mock
    private SyncService syncService;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private TransferService transferService;

    private UserAccountResponseDto mockUserResponse;
    private ExchangeRatesResponseDto mockExchangeRates;
    private ExchangeRateDto mockExchangeRate;
    private Account sourceAccount;
    private Account destinationAccount;
    private AccountDto destinationAccountDto;
    private Currency usdCurrency;
    private Currency eurCurrency;

    @BeforeEach
    void setUp() {
        // Setup currencies
        usdCurrency = new Currency();
        usdCurrency.setId(1L);
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");

        eurCurrency = new Currency();
        eurCurrency.setId(2L);
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");

        // Setup mock user response
        mockUserResponse = new UserAccountResponseDto();
        mockUserResponse.setUserId(1L);
        mockUserResponse.setUsername("testuser");

        // Setup mock exchange rate
        mockExchangeRate = new ExchangeRateDto();
        mockExchangeRate.setFromCurrency("USD");
        mockExchangeRate.setToCurrency("EUR");
        mockExchangeRate.setBuyRate(new BigDecimal("0.85"));
        mockExchangeRate.setSellRate(new BigDecimal("0.85"));

        // Setup mock exchange rates response
        mockExchangeRates = new ExchangeRatesResponseDto();
        mockExchangeRates.setRates(List.of(mockExchangeRate));

        // Setup source account
        sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAmount(new BigDecimal("1000.00"));
        sourceAccount.setCurrency(usdCurrency);

        // Setup destination account
        destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setAmount(new BigDecimal("500.00"));
        destinationAccount.setCurrency(eurCurrency);

        // Setup destination account DTO
        destinationAccountDto = new AccountDto();
        destinationAccountDto.setAccountId(2L);
        destinationAccountDto.setCurrencyCode("EUR");
    }

    @Test
    void transfer_SuccessfulTransfer_ReturnsUpdatedUserResponse() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        // Mock fraud check - valid operation
        when(restTemplate.exchange(
                contains("blocker/check"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Boolean.class)
        )).thenReturn(ResponseEntity.ok(true));

        // Mock exchange rates service
        when(restTemplate.exchange(
                contains("exchange/course"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ExchangeRatesResponseDto.class)
        )).thenReturn(ResponseEntity.ok(mockExchangeRates));

        // Mock account repository
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(destinationAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock account service
        when(accountService.getAccountByUsernameAndCurrencyCode(username, toCurrency))
                .thenReturn(destinationAccountDto);

        // Mock session service
        when(sessionService.updateSession(sessionId)).thenReturn(mockUserResponse);

        // Act
        UserAccountResponseDto result = transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertNotNull(result);
        assertEquals(mockUserResponse.getUserId(), result.getUserId());
        assertEquals(mockUserResponse.getUsername(), result.getUsername());

        // Verify account balances were updated
        verify(accountRepo, times(2)).save(any(Account.class));
        
        // Verify source account balance decreased
        assertEquals(new BigDecimal("900.00"), sourceAccount.getAmount());
        
        // Verify destination account balance increased (converted amount)
        BigDecimal expectedConvertedAmount = amount.multiply(mockExchangeRate.getSellRate());
        assertEquals(new BigDecimal("500.00").add(expectedConvertedAmount), destinationAccount.getAmount());

        // Verify sync service was called
        verify(syncService).syncTransfer(sourceAccount, destinationAccount);

        // Verify notification was sent for success
        verify(notificationService).sendTransferNotification(
                eq(mockUserResponse.getUserId().toString()),
                eq(mockUserResponse.getUsername()),
                eq(amount),
                eq(fromCurrency),
                eq(toCurrency),
                eq(username),
                eq(expectedConvertedAmount),
                eq(true)
        );
    }

    @Test
    void transfer_InvalidOperation_ReturnsNullAndSendsFailureNotification() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        // Mock fraud check - invalid operation
        when(restTemplate.exchange(
                contains("blocker/check"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Boolean.class)
        )).thenReturn(ResponseEntity.ok(false));

        // Act
        UserAccountResponseDto result = transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertNull(result);

        // Verify no account operations were performed
        verify(accountRepo, never()).findById(any());
        verify(accountRepo, never()).save(any());
        verify(syncService, never()).syncTransfer(any(), any());
        verify(sessionService, never()).updateSession(any());

        // Verify notification was sent for failure
        verify(notificationService).sendTransferNotification(
                isNull(),
                isNull(),
                eq(amount),
                eq(fromCurrency),
                eq(toCurrency),
                eq(username),
                isNull(),
                eq(false)
        );
    }

    @Test
    void transfer_InsufficientBalance_ThrowsRuntimeException() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("1500.00"); // More than available balance
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        // Mock fraud check - valid operation
        when(restTemplate.exchange(
                contains("blocker/check"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Boolean.class)
        )).thenReturn(ResponseEntity.ok(true));

        // Mock exchange rates service
        when(restTemplate.exchange(
                contains("exchange/course"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ExchangeRatesResponseDto.class)
        )).thenReturn(ResponseEntity.ok(mockExchangeRates));

        // Mock account repository
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(sourceAccount));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username)
        );

        assertTrue(exception.getMessage().contains("Insufficient balance"));

        // Verify failure notification was sent
        verify(notificationService).sendTransferNotification(
                isNull(),
                isNull(),
                eq(amount),
                eq(fromCurrency),
                eq(toCurrency),
                eq(username),
                isNull(),
                eq(false)
        );
    }

    @Test
    void transfer_SourceAccountNotFound_ThrowsRuntimeException() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 999L; // Non-existent account
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        // Mock fraud check - valid operation
        when(restTemplate.exchange(
                contains("blocker/check"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Boolean.class)
        )).thenReturn(ResponseEntity.ok(true));

        // Mock exchange rates service
        when(restTemplate.exchange(
                contains("exchange/course"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ExchangeRatesResponseDto.class)
        )).thenReturn(ResponseEntity.ok(mockExchangeRates));

        // Mock account repository - return empty for source account
        when(accountRepo.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username)
        );

        assertTrue(exception.getMessage().contains("Source account"));

        // Verify failure notification was sent
        verify(notificationService).sendTransferNotification(
                isNull(),
                isNull(),
                eq(amount),
                eq(fromCurrency),
                eq(toCurrency),
                eq(username),
                isNull(),
                eq(false)
        );
    }

    @Test
    void transfer_SameCurrency_PerformsTransferWithoutConversion() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "USD"; // Same currency
        String username = "recipient";

        // Setup destination account with same currency
        destinationAccount.setCurrency(usdCurrency);
        destinationAccountDto.setCurrencyCode("USD");

        // Mock fraud check - valid operation
        when(restTemplate.exchange(
                contains("blocker/check"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Boolean.class)
        )).thenReturn(ResponseEntity.ok(true));

        // Mock account repository
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(destinationAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock account service
        when(accountService.getAccountByUsernameAndCurrencyCode(username, toCurrency))
                .thenReturn(destinationAccountDto);

        // Mock session service
        when(sessionService.updateSession(sessionId)).thenReturn(mockUserResponse);

        // Act
        UserAccountResponseDto result = transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertNotNull(result);

        // Verify same amount was transferred (no conversion)
        assertEquals(new BigDecimal("900.00"), sourceAccount.getAmount());
        assertEquals(new BigDecimal("600.00"), destinationAccount.getAmount());

        // Verify exchange rates service was NOT called for same currency
        verify(restTemplate, never()).exchange(
                contains("exchange/course"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ExchangeRatesResponseDto.class)
        );

        // Verify notification was sent with same amount
        verify(notificationService).sendTransferNotification(
                eq(mockUserResponse.getUserId().toString()),
                eq(mockUserResponse.getUsername()),
                eq(amount),
                eq(fromCurrency),
                eq(toCurrency),
                eq(username),
                eq(amount), // Same amount for same currency
                eq(true)
        );
    }

    @Test
    void transfer_ExchangeRateNotFound_ThrowsRuntimeException() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "GBP"; // Different currency not in mock rates
        String username = "recipient";

        // Mock fraud check - valid operation
        when(restTemplate.exchange(
                contains("blocker/check"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Boolean.class)
        )).thenReturn(ResponseEntity.ok(true));

        // Mock exchange rates service with rates that don't include USD-GBP
        when(restTemplate.exchange(
                contains("exchange/course"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ExchangeRatesResponseDto.class)
        )).thenReturn(ResponseEntity.ok(mockExchangeRates));

        // Mock account repository
        lenient().when(accountRepo.findById(accountId)).thenReturn(Optional.of(sourceAccount));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username)
        );

        assertTrue(exception.getMessage().contains("Exchange rate not found"));

        // Verify failure notification was sent
        verify(notificationService).sendTransferNotification(
                isNull(),
                isNull(),
                eq(amount),
                eq(fromCurrency),
                eq(toCurrency),
                eq(username),
                isNull(),
                eq(false)
        );
    }

    @Test
    void transfer_RestTemplateException_ThrowsRuntimeException() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String username = "recipient";

        // Mock fraud check to throw exception
        when(restTemplate.exchange(
                contains("blocker/check"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Boolean.class)
        )).thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username)
        );

        assertNotNull(exception);

        // Verify failure notification was sent
        verify(notificationService).sendTransferNotification(
                isNull(),
                isNull(),
                eq(amount),
                eq(fromCurrency),
                eq(toCurrency),
                eq(username),
                isNull(),
                eq(false)
        );
    }

    @Test
    void transfer_ReverseExchangeRate_CalculatesCorrectly() {
        // Arrange
        String sessionId = "session123";
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "EUR"; // Reverse of what's in mock rate
        String toCurrency = "USD";
        String username = "recipient";

        // Setup accounts with reversed currencies
        sourceAccount.setCurrency(eurCurrency);
        destinationAccount.setCurrency(usdCurrency);
        destinationAccountDto.setCurrencyCode("USD");

        // Mock fraud check - valid operation
        when(restTemplate.exchange(
                contains("blocker/check"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Boolean.class)
        )).thenReturn(ResponseEntity.ok(true));

        // Mock exchange rates service
        when(restTemplate.exchange(
                contains("exchange/course"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ExchangeRatesResponseDto.class)
        )).thenReturn(ResponseEntity.ok(mockExchangeRates));

        // Mock account repository
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(destinationAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock account service
        when(accountService.getAccountByUsernameAndCurrencyCode(username, toCurrency))
                .thenReturn(destinationAccountDto);

        // Mock session service
        when(sessionService.updateSession(sessionId)).thenReturn(mockUserResponse);

        // Act
        UserAccountResponseDto result = transferService.transfer(sessionId, accountId, amount, fromCurrency, toCurrency, username);

        // Assert
        assertNotNull(result);

        // Verify account balances were updated with reverse rate calculation
        verify(accountRepo, times(2)).save(any(Account.class));
        
        // Verify source account balance decreased
        assertEquals(new BigDecimal("900.00"), sourceAccount.getAmount());
    }
} 
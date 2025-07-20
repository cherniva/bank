package com.cherniva.cashservice.service;

import com.cherniva.common.dto.CashNotificationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class NotificationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    @Test
    void sendCashNotification_SuccessfulDeposit_SendsCorrectNotification() {
        // Act
        notificationService.sendCashNotification("1", "testuser", BigDecimal.valueOf(100), 
                "USD", 1L, "deposit", true);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/cash"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<CashNotificationDto> capturedEntity = entityCaptor.getValue();
        CashNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("deposit", dto.getOperationType());
        assertEquals(BigDecimal.valueOf(100), dto.getAmount());
        assertEquals("USD", dto.getCurrencyCode());
        assertEquals(1L, dto.getAccountId());
        assertTrue(dto.getMessage().contains("успешно"));
    }

    @Test
    void sendCashNotification_SuccessfulWithdraw_SendsCorrectNotification() {
        // Act
        notificationService.sendCashNotification("1", "testuser", BigDecimal.valueOf(50), 
                "EUR", 2L, "withdraw", true);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/cash"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<CashNotificationDto> capturedEntity = entityCaptor.getValue();
        CashNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("withdraw", dto.getOperationType());
        assertEquals(BigDecimal.valueOf(50), dto.getAmount());
        assertEquals("EUR", dto.getCurrencyCode());
        assertEquals(2L, dto.getAccountId());
        assertTrue(dto.getMessage().contains("успешно"));
    }

    @Test
    void sendCashNotification_FailedDepositWithUserInfo_SendsErrorNotification() {
        // Act
        notificationService.sendCashNotification("1", "testuser", BigDecimal.valueOf(100), 
                "USD", 1L, "deposit", false);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/cash"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<CashNotificationDto> capturedEntity = entityCaptor.getValue();
        CashNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("deposit", dto.getOperationType());
        assertTrue(dto.getMessage().contains("Ошибка"));
    }

    @Test
    void sendCashNotification_FailedOperationBlocked_SendsBlockedNotification() {
        // Act
        notificationService.sendCashNotification(null, null, BigDecimal.valueOf(100), 
                "USD", 1L, "deposit", false);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/cash"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<CashNotificationDto> capturedEntity = entityCaptor.getValue();
        CashNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertNull(dto.getUsername());
        assertEquals("deposit", dto.getOperationType());
        assertTrue(dto.getMessage().contains("заблокирована"));
    }
} 
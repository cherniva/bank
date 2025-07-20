package com.cherniva.transferservice.service;

import com.cherniva.common.dto.TransferNotificationDto;
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
    void sendTransferNotification_SuccessfulTransfer_SendsCorrectNotification() {
        // Act
        notificationService.sendTransferNotification("1", "testuser", BigDecimal.valueOf(100), 
                "USD", "EUR", "recipient", BigDecimal.valueOf(85), true);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/transfer"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<TransferNotificationDto> capturedEntity = entityCaptor.getValue();
        TransferNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("transfer", dto.getOperationType());
        assertEquals(BigDecimal.valueOf(100), dto.getAmount());
        assertEquals("USD", dto.getFromCurrency());
        assertEquals("EUR", dto.getToCurrency());
        assertEquals("recipient", dto.getTargetUsername());
        assertEquals(BigDecimal.valueOf(85), dto.getConvertedAmount());
        assertTrue(dto.getMessage().contains("успешно"));
    }

    @Test
    void sendTransferNotification_FailedTransferWithUserInfo_SendsErrorNotification() {
        // Act
        notificationService.sendTransferNotification("1", "testuser", BigDecimal.valueOf(100), 
                "USD", "EUR", "recipient", null, false);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/transfer"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<TransferNotificationDto> capturedEntity = entityCaptor.getValue();
        TransferNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("transfer", dto.getOperationType());
        assertTrue(dto.getMessage().contains("Ошибка"));
    }

    @Test
    void sendTransferNotification_FailedTransferBlocked_SendsBlockedNotification() {
        // Act
        notificationService.sendTransferNotification(null, null, BigDecimal.valueOf(100), 
                "USD", "EUR", "recipient", null, false);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/transfer"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<TransferNotificationDto> capturedEntity = entityCaptor.getValue();
        TransferNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertNull(dto.getUsername());
        assertEquals("transfer", dto.getOperationType());
        assertTrue(dto.getMessage().contains("заблокирован"));
    }
} 
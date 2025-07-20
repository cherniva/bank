package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.AccountsNotificationDto;
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
    void sendEditUserNotification_Success_SendsCorrectNotification() {
        // Act
        notificationService.sendEditUserNotification("1", "testuser", "John", "Doe", "1990-01-01", true);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/accounts"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<AccountsNotificationDto> capturedEntity = entityCaptor.getValue();
        AccountsNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("editUser", dto.getOperationType());
    }

    @Test
    void sendAddAccountNotification_Success_SendsCorrectNotification() {
        // Act
        notificationService.sendAddAccountNotification("1", "testuser", "USD", true);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/accounts"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<AccountsNotificationDto> capturedEntity = entityCaptor.getValue();
        AccountsNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("addAccount", dto.getOperationType());
        assertEquals("USD", dto.getCurrencyCode());
    }

    @Test
    void sendAddAccountNotification_Failure_SendsFailureNotification() {
        // Act
        notificationService.sendAddAccountNotification("1", "testuser", "USD", false);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/accounts"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<AccountsNotificationDto> capturedEntity = entityCaptor.getValue();
        AccountsNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("addAccount", dto.getOperationType());
        assertEquals("USD", dto.getCurrencyCode());
    }

    @Test
    void sendDeleteAccountNotification_Success_SendsCorrectNotification() {
        // Act
        notificationService.sendDeleteAccountNotification("1", "testuser", "USD", true);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("lb://api-gateway/notifications/accounts"), 
                                    eq(HttpMethod.POST), 
                                    entityCaptor.capture(), 
                                    eq(Void.class));

        HttpEntity<AccountsNotificationDto> capturedEntity = entityCaptor.getValue();
        AccountsNotificationDto dto = capturedEntity.getBody();
        
        assertNotNull(dto);
        assertEquals("1", dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("deleteAccount", dto.getOperationType());
        assertEquals("USD", dto.getCurrencyCode());
    }
} 
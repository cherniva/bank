package com.cherniva.notificationsservice.controller;

import com.cherniva.common.dto.*;
import com.cherniva.notificationsservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        lenient().when(notificationService.generateAccountsMessage(any())).thenReturn("Test accounts message");
        lenient().when(notificationService.generateTransferMessage(any())).thenReturn("Test transfer message");
        lenient().when(notificationService.generateCashMessage(any())).thenReturn("Test cash message");
        lenient().when(notificationService.createNotification(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(createTestNotification());
    }

    @Test
    void receiveAccountsNotification_ValidDto_ReturnsOk() {
        // Arrange
        AccountsNotificationDto dto = new AccountsNotificationDto();
        dto.setUserId("1");
        dto.setUsername("testuser");
        dto.setOperationType("addAccount");

        // Act
        ResponseEntity<Void> response = notificationController.receiveAccountsNotification(dto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(notificationService).generateAccountsMessage(dto);
        verify(notificationService).createNotification(eq("1"), eq("testuser"), eq("accounts"), 
                eq("addAccount"), eq("Test accounts message"));
        verify(notificationService).storeNotification(any(NotificationDto.class));
    }

    @Test
    void receiveAccountsNotification_ExceptionThrown_ReturnsBadRequest() {
        // Arrange
        AccountsNotificationDto dto = new AccountsNotificationDto();
        when(notificationService.generateAccountsMessage(any())).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<Void> response = notificationController.receiveAccountsNotification(dto);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void receiveTransferNotification_ValidDto_ReturnsOk() {
        // Arrange
        TransferNotificationDto dto = new TransferNotificationDto();
        dto.setUserId("1");
        dto.setUsername("testuser");
        dto.setOperationType("transfer");

        // Act
        ResponseEntity<Void> response = notificationController.receiveTransferNotification(dto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(notificationService).generateTransferMessage(dto);
        verify(notificationService).createNotification(eq("1"), eq("testuser"), eq("transfer"), 
                eq("transfer"), eq("Test transfer message"));
        verify(notificationService).storeNotification(any(NotificationDto.class));
    }

    @Test
    void receiveTransferNotification_ExceptionThrown_ReturnsBadRequest() {
        // Arrange
        TransferNotificationDto dto = new TransferNotificationDto();
        when(notificationService.generateTransferMessage(any())).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<Void> response = notificationController.receiveTransferNotification(dto);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void receiveCashNotification_ValidDto_ReturnsOk() {
        // Arrange
        CashNotificationDto dto = new CashNotificationDto();
        dto.setUserId("1");
        dto.setUsername("testuser");
        dto.setOperationType("deposit");

        // Act
        ResponseEntity<Void> response = notificationController.receiveCashNotification(dto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(notificationService).generateCashMessage(dto);
        verify(notificationService).createNotification(eq("1"), eq("testuser"), eq("cash"), 
                eq("deposit"), eq("Test cash message"));
        verify(notificationService).storeNotification(any(NotificationDto.class));
    }

    @Test
    void receiveCashNotification_ExceptionThrown_ReturnsBadRequest() {
        // Arrange
        CashNotificationDto dto = new CashNotificationDto();
        when(notificationService.generateCashMessage(any())).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<Void> response = notificationController.receiveCashNotification(dto);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void getUserNotifications_ValidUserId_ReturnsNotifications() {
        // Arrange
        List<NotificationDto> mockNotifications = List.of(createTestNotification());
        when(notificationService.getUserNotifications("1")).thenReturn(mockNotifications);

        // Act
        ResponseEntity<List<NotificationDto>> response = notificationController.getUserNotifications("1");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(notificationService).getUserNotifications("1");
    }

    @Test
    void getUserNotifications_ExceptionThrown_ReturnsBadRequest() {
        // Arrange
        when(notificationService.getUserNotifications("1")).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<List<NotificationDto>> response = notificationController.getUserNotifications("1");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void markNotificationAsRead_ValidId_ReturnsOk() {
        // Act
        ResponseEntity<Void> response = notificationController.markNotificationAsRead("notification123");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(notificationService).markNotificationAsRead("notification123");
    }

    @Test
    void markNotificationAsRead_ExceptionThrown_ReturnsBadRequest() {
        // Arrange
        doThrow(new RuntimeException("Error")).when(notificationService).markNotificationAsRead("notification123");

        // Act
        ResponseEntity<Void> response = notificationController.markNotificationAsRead("notification123");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }

    private NotificationDto createTestNotification() {
        NotificationDto notification = new NotificationDto();
        notification.setId("test-id");
        notification.setUserId("1");
        notification.setUsername("testuser");
        notification.setServiceType("test");
        notification.setOperationType("test-op");
        notification.setMessage("Test message");
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        return notification;
    }
} 
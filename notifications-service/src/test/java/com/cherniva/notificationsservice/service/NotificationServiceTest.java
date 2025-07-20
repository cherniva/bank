package com.cherniva.notificationsservice.service;

import com.cherniva.common.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // Clear any existing notifications before each test
        notificationService = new NotificationService();
    }

    @Test
    void storeNotification_ValidNotification_StoresSuccessfully() {
        // Arrange
        NotificationDto notification = createTestNotification("1", "testuser", "test", "operation", "Test message");

        // Act
        notificationService.storeNotification(notification);

        // Assert
        List<NotificationDto> userNotifications = notificationService.getUserNotifications("1");
        assertEquals(1, userNotifications.size());
        assertEquals(notification.getId(), userNotifications.get(0).getId());
    }

    @Test
    void getUserNotifications_ExistingUser_ReturnsUnreadNotifications() {
        // Arrange
        NotificationDto notification1 = createTestNotification("1", "testuser", "service1", "op1", "Message 1");
        NotificationDto notification2 = createTestNotification("1", "testuser", "service2", "op2", "Message 2");
        notification2.setRead(true); // Mark as read
        
        notificationService.storeNotification(notification1);
        notificationService.storeNotification(notification2);

        // Act
        List<NotificationDto> userNotifications = notificationService.getUserNotifications("1");

        // Assert
        assertEquals(1, userNotifications.size()); // Only unread notifications
        assertEquals(notification1.getId(), userNotifications.get(0).getId());
    }

    @Test
    void getUserNotifications_NonexistentUser_ReturnsEmptyList() {
        // Act
        List<NotificationDto> userNotifications = notificationService.getUserNotifications("nonexistent");

        // Assert
        assertTrue(userNotifications.isEmpty());
    }

    @Test
    void getUserNotifications_MultipleNotifications_ReturnsSortedByTimestamp() throws InterruptedException {
        // Arrange
        NotificationDto notification1 = createTestNotification("1", "testuser", "service1", "op1", "Message 1");
        Thread.sleep(1); // Ensure different timestamps
        NotificationDto notification2 = createTestNotification("1", "testuser", "service2", "op2", "Message 2");
        
        notificationService.storeNotification(notification1);
        notificationService.storeNotification(notification2);

        // Act
        List<NotificationDto> userNotifications = notificationService.getUserNotifications("1");

        // Assert
        assertEquals(2, userNotifications.size());
        // Should be sorted by timestamp (newest first)
        assertTrue(userNotifications.get(0).getTimestamp().isAfter(userNotifications.get(1).getTimestamp()) ||
                  userNotifications.get(0).getTimestamp().equals(userNotifications.get(1).getTimestamp()));
    }

    @Test
    void markNotificationAsRead_ExistingNotification_MarksAsRead() {
        // Arrange
        NotificationDto notification = createTestNotification("1", "testuser", "test", "operation", "Test message");
        notificationService.storeNotification(notification);

        // Act
        notificationService.markNotificationAsRead(notification.getId());

        // Assert
        List<NotificationDto> userNotifications = notificationService.getUserNotifications("1");
        assertTrue(userNotifications.isEmpty()); // Should be empty since notification is now read
    }

    @Test
    void markNotificationAsRead_NonexistentNotification_DoesNotThrowException() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> notificationService.markNotificationAsRead("nonexistent-id"));
    }

    @Test
    void createNotification_ValidParameters_CreatesNotification() {
        // Act
        NotificationDto notification = notificationService.createNotification(
                "1", "testuser", "serviceType", "operationType", "Test message");

        // Assert
        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertEquals("1", notification.getUserId());
        assertEquals("testuser", notification.getUsername());
        assertEquals("serviceType", notification.getServiceType());
        assertEquals("operationType", notification.getOperationType());
        assertEquals("Test message", notification.getMessage());
        assertNotNull(notification.getTimestamp());
        assertFalse(notification.isRead());
    }

    @Test
    void generateAccountsMessage_AddAccountOperation_GeneratesCorrectMessage() {
        // Arrange
        AccountsNotificationDto dto = new AccountsNotificationDto();
        dto.setOperationType("addAccount");
        dto.setCurrencyCode("USD");

        // Act
        String message = notificationService.generateAccountsMessage(dto);

        // Assert
        assertEquals("Счет в валюте USD успешно создан", message);
    }

    @Test
    void generateAccountsMessage_DepositOperation_GeneratesCorrectMessage() {
        // Arrange
        AccountsNotificationDto dto = new AccountsNotificationDto();
        dto.setOperationType("deposit");
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setCurrencyCode("EUR");

        // Act
        String message = notificationService.generateAccountsMessage(dto);

        // Assert
        assertEquals("Пополнение счета на сумму 100 EUR", message);
    }

    @Test
    void generateAccountsMessage_WithCustomMessage_ReturnsCustomMessage() {
        // Arrange
        AccountsNotificationDto dto = new AccountsNotificationDto();
        dto.setOperationType("addAccount");
        dto.setMessage("Custom message");

        // Act
        String message = notificationService.generateAccountsMessage(dto);

        // Assert
        assertEquals("Custom message", message);
    }

    @Test
    void generateTransferMessage_WithCustomMessage_ReturnsCustomMessage() {
        // Arrange
        TransferNotificationDto dto = new TransferNotificationDto();
        dto.setMessage("Custom transfer message");

        // Act
        String message = notificationService.generateTransferMessage(dto);

        // Assert
        assertEquals("Custom transfer message", message);
    }

    @Test
    void generateTransferMessage_GeneratesCorrectMessage() {
        // Arrange
        TransferNotificationDto dto = new TransferNotificationDto();
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setFromCurrency("USD");
        dto.setConvertedAmount(BigDecimal.valueOf(85));
        dto.setToCurrency("EUR");
        dto.setTargetUsername("recipient");

        // Act
        String message = notificationService.generateTransferMessage(dto);

        // Assert
        assertEquals("Перевод 100 USD в 85 EUR пользователю recipient", message);
    }

    @Test
    void generateCashMessage_DepositOperation_GeneratesCorrectMessage() {
        // Arrange
        CashNotificationDto dto = new CashNotificationDto();
        dto.setOperationType("deposit");
        dto.setAmount(BigDecimal.valueOf(50));
        dto.setCurrencyCode("USD");

        // Act
        String message = notificationService.generateCashMessage(dto);

        // Assert
        assertEquals("Пополнение наличными на сумму 50 USD", message);
    }

    @Test
    void generateCashMessage_WithdrawOperation_GeneratesCorrectMessage() {
        // Arrange
        CashNotificationDto dto = new CashNotificationDto();
        dto.setOperationType("withdraw");
        dto.setAmount(BigDecimal.valueOf(25));
        dto.setCurrencyCode("EUR");

        // Act
        String message = notificationService.generateCashMessage(dto);

        // Assert
        assertEquals("Снятие наличными суммы 25 EUR", message);
    }

    private NotificationDto createTestNotification(String userId, String username, String serviceType, 
                                                  String operationType, String message) {
        NotificationDto notification = new NotificationDto();
        notification.setId(java.util.UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setUsername(username);
        notification.setServiceType(serviceType);
        notification.setOperationType(operationType);
        notification.setMessage(message);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        return notification;
    }
} 
package com.cherniva.notificationsservice.service;

import com.cherniva.common.dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    // In-memory storage for notifications (in production, this would be a database)
    private final ConcurrentMap<String, List<NotificationDto>> userNotifications = new ConcurrentHashMap<>();
    
    public void storeNotification(NotificationDto notification) {
        userNotifications.computeIfAbsent(notification.getUserId(), k -> new ArrayList<>())
                .add(notification);
    }
    
    public List<NotificationDto> getUserNotifications(String userId) {
        List<NotificationDto> notifications = userNotifications.getOrDefault(userId, new ArrayList<>());
        
        // Only return unread notifications, sorted by timestamp (newest first)
        return notifications.stream()
                .filter(notification -> !notification.isRead())
                .sorted((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    public void markNotificationAsRead(String notificationId) {
        // Find and mark the notification as read
        for (List<NotificationDto> notifications : userNotifications.values()) {
            for (NotificationDto notification : notifications) {
                if (notificationId.equals(notification.getId())) {
                    notification.setRead(true);
                    return;
                }
            }
        }
    }
    
    public NotificationDto createNotification(String userId, String username, String serviceType, 
                                           String operationType, String message) {
        NotificationDto notification = new NotificationDto();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setUsername(username);
        notification.setServiceType(serviceType);
        notification.setOperationType(operationType);
        notification.setMessage(message);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        
        return notification;
    }
    
    public String generateAccountsMessage(AccountsNotificationDto dto) {
        if (dto.getMessage() != null && !dto.getMessage().trim().isEmpty()) {
            return dto.getMessage();
        }
        
        switch (dto.getOperationType()) {
            case "addAccount":
                return String.format("Счет в валюте %s успешно создан", dto.getCurrencyCode());
            case "deposit":
                return String.format("Пополнение счета на сумму %s %s", dto.getAmount(), dto.getCurrencyCode());
            case "withdraw":
                return String.format("Снятие со счета суммы %s %s", dto.getAmount(), dto.getCurrencyCode());
            case "transfer":
                return String.format("Перевод суммы %s %s пользователю %s", dto.getAmount(), dto.getCurrencyCode(), dto.getTargetUsername());
            default:
                return "Операция со счетом выполнена";
        }
    }
    
    public String generateTransferMessage(TransferNotificationDto dto) {
        if (dto.getMessage() != null && !dto.getMessage().trim().isEmpty()) {
            return dto.getMessage();
        }
        
        return String.format("Перевод %s %s в %s %s пользователю %s", 
                dto.getAmount(), dto.getFromCurrency(), 
                dto.getConvertedAmount(), dto.getToCurrency(), 
                dto.getTargetUsername());
    }
    
    public String generateCashMessage(CashNotificationDto dto) {
        if (dto.getMessage() != null && !dto.getMessage().trim().isEmpty()) {
            return dto.getMessage();
        }
        
        switch (dto.getOperationType()) {
            case "deposit":
                return String.format("Пополнение наличными на сумму %s %s", dto.getAmount(), dto.getCurrencyCode());
            case "withdraw":
                return String.format("Снятие наличными суммы %s %s", dto.getAmount(), dto.getCurrencyCode());
            default:
                return "Операция с наличными выполнена";
        }
    }
} 
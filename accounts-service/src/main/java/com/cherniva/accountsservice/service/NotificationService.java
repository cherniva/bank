package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.AccountsNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final RestTemplate restTemplate;
    
    public void sendEditUserNotification(String userId, String username, String name, String surname, String birthdate, boolean success) {
        try {
            AccountsNotificationDto notificationDto = new AccountsNotificationDto();
            notificationDto.setUserId(userId);
            notificationDto.setUsername(username);
            notificationDto.setOperationType("editUser");
            
            // Set appropriate message based on success/failure
            if (success) {
                StringBuilder messageBuilder = new StringBuilder("Данные профиля обновлены: ");
                boolean hasChanges = false;
                
                if (name != null && !name.trim().isEmpty()) {
                    messageBuilder.append("имя: ").append(name.trim());
                    hasChanges = true;
                }
                
                if (surname != null && !surname.trim().isEmpty()) {
                    if (hasChanges) {
                        messageBuilder.append(", ");
                    }
                    messageBuilder.append("фамилия: ").append(surname.trim());
                    hasChanges = true;
                }
                
                if (birthdate != null && !birthdate.trim().isEmpty()) {
                    if (hasChanges) {
                        messageBuilder.append(", ");
                    }
                    messageBuilder.append("дата рождения: ").append(birthdate.trim());
                    hasChanges = true;
                }
                
                if (!hasChanges) {
                    messageBuilder = new StringBuilder("Попытка обновления данных профиля (изменения не обнаружены)");
                }
                
                notificationDto.setMessage(messageBuilder.toString());
            } else {
                notificationDto.setMessage("Ошибка при обновлении данных профиля");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AccountsNotificationDto> requestEntity = new HttpEntity<>(notificationDto, headers);
            
            restTemplate.exchange(
                    "lb://api-gateway/notifications/accounts",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );
            
            if (success) {
                log.info("Edit user notification sent for user: {}", username);
            } else {
                log.info("Edit user failure notification sent for user: {}", username);
            }
                    
        } catch (Exception e) {
            log.error("Failed to send edit user notification", e);
            // Don't throw exception to avoid breaking the main edit user flow
        }
    }
    
    public void sendAddAccountNotification(String userId, String username, String currencyCode, boolean success) {
        try {
            AccountsNotificationDto notificationDto = new AccountsNotificationDto();
            notificationDto.setUserId(userId);
            notificationDto.setUsername(username);
            notificationDto.setOperationType("addAccount");
            notificationDto.setCurrencyCode(currencyCode);
            
            // Set appropriate message based on success/failure
            if (success) {
                notificationDto.setMessage(String.format("Счет в валюте %s успешно создан", currencyCode));
            } else {
                notificationDto.setMessage(String.format("Ошибка при создании счета в валюте %s", currencyCode));
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AccountsNotificationDto> requestEntity = new HttpEntity<>(notificationDto, headers);
            
            restTemplate.exchange(
                    "lb://api-gateway/notifications/accounts",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );
            
            if (success) {
                log.info("Add account notification sent for user: {}, currency: {}", username, currencyCode);
            } else {
                log.info("Add account failure notification sent for user: {}, currency: {}", username, currencyCode);
            }
                    
        } catch (Exception e) {
            log.error("Failed to send add account notification", e);
            // Don't throw exception to avoid breaking the main add account flow
        }
    }
} 
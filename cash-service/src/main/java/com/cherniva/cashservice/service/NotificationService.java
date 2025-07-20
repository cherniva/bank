package com.cherniva.cashservice.service;

import com.cherniva.common.dto.CashNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final RestTemplate restTemplate;
    
    public void sendCashNotification(String userId, String username, BigDecimal amount, 
                                   String currencyCode, Long accountId, String operationType, boolean success) {
        try {
            CashNotificationDto notificationDto = new CashNotificationDto();
            notificationDto.setUserId(userId);
            notificationDto.setUsername(username);
            notificationDto.setOperationType(operationType);
            notificationDto.setAmount(amount);
            notificationDto.setCurrencyCode(currencyCode);
            notificationDto.setAccountId(accountId);
            
            // Set appropriate message based on success/failure
            if (success) {
                if ("deposit".equals(operationType)) {
                    notificationDto.setMessage(String.format("Пополнение наличными на сумму %s %s выполнено успешно", 
                            amount, currencyCode));
                } else if ("withdraw".equals(operationType)) {
                    notificationDto.setMessage(String.format("Снятие наличными суммы %s %s выполнено успешно", 
                            amount, currencyCode));
                }
            } else {
                if (userId == null || username == null) {
                    notificationDto.setMessage("Операция с наличными заблокирована системой безопасности");
                } else {
                    if ("deposit".equals(operationType)) {
                        notificationDto.setMessage(String.format("Ошибка при пополнении наличными на сумму %s %s", 
                                amount, currencyCode));
                    } else if ("withdraw".equals(operationType)) {
                        notificationDto.setMessage(String.format("Ошибка при снятии наличными суммы %s %s", 
                                amount, currencyCode));
                    }
                }
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<CashNotificationDto> requestEntity = new HttpEntity<>(notificationDto, headers);
            
            restTemplate.exchange(
                    "lb://api-gateway/notifications/cash",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );
            
            if (success) {
                log.info("Cash {} notification sent for user: {}, amount: {} {}", 
                        operationType, username, amount, currencyCode);
            } else {
                log.info("Cash {} failure notification sent for amount: {} {}", 
                        operationType, amount, currencyCode);
            }
                    
        } catch (Exception e) {
            log.error("Failed to send cash notification", e);
            // Don't throw exception to avoid breaking the main cash operation flow
        }
    }
} 
package com.cherniva.transferservice.service;

import com.cherniva.common.dto.TransferNotificationDto;
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
    
    public void sendTransferNotification(String userId, String username, BigDecimal amount, 
                                       String fromCurrency, String toCurrency, String targetUsername, 
                                       BigDecimal convertedAmount, boolean success) {
        try {
            TransferNotificationDto notificationDto = new TransferNotificationDto();
            notificationDto.setUserId(userId);
            notificationDto.setUsername(username);
            notificationDto.setOperationType("transfer");
            notificationDto.setAmount(amount);
            notificationDto.setFromCurrency(fromCurrency);
            notificationDto.setToCurrency(toCurrency);
            notificationDto.setTargetUsername(targetUsername);
            notificationDto.setConvertedAmount(convertedAmount);
            
            // Set appropriate message based on success/failure
            if (success) {
                notificationDto.setMessage(String.format("Перевод %s %s в %s %s пользователю %s выполнен успешно", 
                        amount, fromCurrency, convertedAmount, toCurrency, targetUsername));
            } else {
                if (userId == null || username == null) {
                    notificationDto.setMessage("Перевод заблокирован системой безопасности");
                } else {
                    notificationDto.setMessage(String.format("Ошибка при выполнении перевода %s %s пользователю %s", 
                            amount, fromCurrency, targetUsername));
                }
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<TransferNotificationDto> requestEntity = new HttpEntity<>(notificationDto, headers);
            
            restTemplate.exchange(
                    "lb://api-gateway/notifications/transfer",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );
            
            if (success) {
                log.info("Transfer notification sent for user: {}, amount: {} {} to {} {}", 
                        username, amount, fromCurrency, convertedAmount, toCurrency);
            } else {
                log.info("Transfer failure notification sent for amount: {} {} to {}", 
                        amount, fromCurrency, targetUsername);
            }
                    
        } catch (Exception e) {
            log.error("Failed to send transfer notification", e);
            // Don't throw exception to avoid breaking the main transfer flow
        }
    }
} 
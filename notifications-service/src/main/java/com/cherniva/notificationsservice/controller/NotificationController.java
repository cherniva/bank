package com.cherniva.notificationsservice.controller;

import com.cherniva.common.dto.*;
import com.cherniva.notificationsservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;

    // Endpoint for receiving notifications from accounts service
    @PostMapping("/accounts")
    public ResponseEntity<Void> receiveAccountsNotification(@RequestBody AccountsNotificationDto notificationDto) {
        try {
            String message = notificationService.generateAccountsMessage(notificationDto);
            NotificationDto notification = notificationService.createNotification(
                    notificationDto.getUserId(),
                    notificationDto.getUsername(),
                    "accounts",
                    notificationDto.getOperationType(),
                    message
            );
            log.info("{}", notification);
            
            notificationService.storeNotification(notification);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint for receiving notifications from transfer service
    @PostMapping("/transfer")
    public ResponseEntity<Void> receiveTransferNotification(@RequestBody TransferNotificationDto notificationDto) {
        try {
            String message = notificationService.generateTransferMessage(notificationDto);
            NotificationDto notification = notificationService.createNotification(
                    notificationDto.getUserId(),
                    notificationDto.getUsername(),
                    "transfer",
                    notificationDto.getOperationType(),
                    message
            );
            log.info("{}", notification);
            
            notificationService.storeNotification(notification);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint for receiving notifications from cash service
    @PostMapping("/cash")
    public ResponseEntity<Void> receiveCashNotification(@RequestBody CashNotificationDto notificationDto) {
        try {
            String message = notificationService.generateCashMessage(notificationDto);
            NotificationDto notification = notificationService.createNotification(
                    notificationDto.getUserId(),
                    notificationDto.getUsername(),
                    "cash",
                    notificationDto.getOperationType(),
                    message
            );
            log.info("{}", notification);
            
            notificationService.storeNotification(notification);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint for retrieving notifications by frontend
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getUserNotifications(@PathVariable String userId) {
        try {
            List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint to mark a notification as read
    @PostMapping("/mark-read/{notificationId}")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

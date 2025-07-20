package com.cherniva.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private String id;
    private String userId;
    private String username;
    private String message;
    private String serviceType; // "accounts", "transfer", "cash"
    private String operationType; // "deposit", "withdraw", "transfer", "addAccount", etc.
    private LocalDateTime timestamp;
    private boolean read;
} 
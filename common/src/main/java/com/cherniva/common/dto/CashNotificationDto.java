package com.cherniva.common.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CashNotificationDto {
    private String userId;
    private String username;
    private String operationType; // "deposit", "withdraw"
    private BigDecimal amount;
    private String currencyCode;
    private Long accountId;
    private String message;
} 
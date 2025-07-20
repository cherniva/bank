package com.cherniva.common.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferNotificationDto {
    private String userId;
    private String username;
    private String operationType; // "transfer"
    private BigDecimal amount;
    private String fromCurrency;
    private String toCurrency;
    private String targetUsername;
    private BigDecimal convertedAmount;
    private String message;
} 
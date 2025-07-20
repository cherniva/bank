package com.cherniva.common.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountsNotificationDto {
    private String userId;
    private String username;
    private String operationType; // "addAccount", "deposit", "withdraw", "transfer"
    private BigDecimal amount;
    private String currencyCode;
    private Long accountId;
    private String targetUsername; // for transfers
    private String message;
} 
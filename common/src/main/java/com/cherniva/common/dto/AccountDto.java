package com.cherniva.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDto {
    private Long accountId;
    private Long userDetailsId;
    private String currencyCode;
    private String currencyName;
    private BigDecimal amount;
    private boolean active;
}

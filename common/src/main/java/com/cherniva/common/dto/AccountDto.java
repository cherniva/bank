package com.cherniva.common.dto;

import lombok.Data;

@Data
public class AccountDto {
    private Long accountId;
    private String currencyCode;
    private String currencyName;
}

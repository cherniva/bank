package com.cherniva.common.dto;

import lombok.Data;

@Data
public class TransferDto {
    private AccountDto sourceAccount;
    private AccountDto destinationAccount;
}

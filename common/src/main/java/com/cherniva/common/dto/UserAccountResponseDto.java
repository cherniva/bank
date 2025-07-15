package com.cherniva.common.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class UserAccountResponseDto {
    private Long userId;
    private String username;
    private String name;
    private String surname;
    private LocalDate birthday;
    private List<AccountDto> accounts;
    private String sessionId;
} 
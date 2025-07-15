package com.cherniva.common.dto;

import lombok.Data;

@Data
public class SessionValidationDto {
    private String sessionId;
    private String username;
    private Long userId;
    private boolean valid;
} 
package com.cherniva.common.dto;

import lombok.Data;

@Data
public class SessionValidationDto {
    private String sessionId;
    private UserAccountResponseDto userData;
    private boolean valid;
    
    // Convenience methods for easy access to user data
    public String getUsername() {
        return userData != null ? userData.getUsername() : null;
    }
    
    public Long getUserId() {
        return userData != null ? userData.getUserId() : null;
    }
    
    public String getName() {
        return userData != null ? userData.getName() : null;
    }
    
    public String getSurname() {
        return userData != null ? userData.getSurname() : null;
    }
    
    public java.time.LocalDate getBirthday() {
        return userData != null ? userData.getBirthdate() : null;
    }
    
    public java.util.List<AccountDto> getAccounts() {
        return userData != null ? userData.getAccounts() : null;
    }
} 
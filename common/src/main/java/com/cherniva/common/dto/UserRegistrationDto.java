package com.cherniva.common.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private String name;
    private String surname;
    private LocalDate birthdate;
}

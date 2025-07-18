package com.cherniva.frontui.controller;

import com.cherniva.common.dto.SessionValidationDto;
import com.cherniva.frontui.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import com.cherniva.common.dto.UserAccountResponseDto;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final SessionService sessionService;

    @GetMapping({"/", "/main"})
    public String mainPage(@CookieValue(value = "sessionId", required = false) String sessionId, Model model) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (sessionValidation.isValid()) {
                populateModelWithUserData(model, sessionValidation);
                return "main";
            }
        }
//        SessionValidationDto sessionValidation = new SessionValidationDto();
//        sessionValidation.setUsername("admin");
//        sessionValidation.setUserId(1L);
//        populateModelWithUserData(model, sessionValidation);
//        return "main";

        // User is not authenticated
        model.addAttribute("authenticated", false);
        return "redirect:/login";
    }
    
    private void populateModelWithUserData(Model model, SessionValidationDto sessionValidation) {
        // Basic user information
        model.addAttribute("username", sessionValidation.getUsername());
        model.addAttribute("userId", sessionValidation.getUserId());
        model.addAttribute("authenticated", true);
        
        // Real user data from session
        model.addAttribute("name", getFullName(sessionValidation));
        model.addAttribute("birthdate", sessionValidation.getBirthday() != null ? 
            sessionValidation.getBirthday().toString() : "N/A");
        
        // Real accounts data from session
        List<MockAccount> accounts = new ArrayList<>();
        if (sessionValidation.getAccounts() != null && !sessionValidation.getAccounts().isEmpty()) {
            for (com.cherniva.common.dto.AccountDto accountDto : sessionValidation.getAccounts()) {
                accounts.add(new MockAccount(
                    accountDto.getCurrencyCode(), 
                    accountDto.getCurrencyName(), 
                    0.0, // Balance would need to be fetched from accounts service
                    true
                ));
            }
        } else {
            // Fallback to mock accounts if no real accounts exist
            accounts.add(new MockAccount("USD", "Доллар США", -1, true));
            accounts.add(new MockAccount("EUR", "Евро", -1, true));
            accounts.add(new MockAccount("RUB", "Рубль", -1, true));
        }
        model.addAttribute("accounts", accounts);
        
        // Mock currencies for dropdowns (this could be fetched from a service)
        List<MockCurrency> currencies = new ArrayList<>();
        currencies.add(new MockCurrency("USD", "Доллар США"));
        currencies.add(new MockCurrency("EUR", "Евро"));
        currencies.add(new MockCurrency("RUB", "Рубль"));
        model.addAttribute("currency", currencies);
        
        // Mock users for transfer dropdown (this could be fetched from a service)
        List<MockUser> users = new ArrayList<>();
        users.add(new MockUser("john.doe", "Джон Доу"));
        users.add(new MockUser("jane.smith", "Джейн Смит"));
        model.addAttribute("users", users);
        
        // Error attributes (empty for now)
        model.addAttribute("passwordErrors", null);
        model.addAttribute("userAccountsErrors", null);
        model.addAttribute("cashErrors", null);
        model.addAttribute("transferErrors", null);
        model.addAttribute("transferOtherErrors", null);
    }
    
    private String getFullName(SessionValidationDto sessionValidation) {
        if (sessionValidation.getName() != null && sessionValidation.getSurname() != null) {
            return sessionValidation.getName() + " " + sessionValidation.getSurname();
        } else if (sessionValidation.getName() != null) {
            return sessionValidation.getName();
        } else if (sessionValidation.getSurname() != null) {
            return sessionValidation.getSurname();
        } else {
            return sessionValidation.getUsername(); // Fallback to username
        }
    }
    
    // Mock classes for demonstration
    public static class MockAccount {
        private String currencyCode;
        private String currencyTitle;
        private double value;
        private boolean exists;
        
        public MockAccount(String currencyCode, String currencyTitle, double value, boolean exists) {
            this.currencyCode = currencyCode;
            this.currencyTitle = currencyTitle;
            this.value = value;
            this.exists = exists;
        }
        
        public MockCurrency getCurrency() {
            return new MockCurrency(currencyCode, currencyTitle);
        }
        
        public double getValue() {
            return value;
        }
        
        public boolean getExists() {
            return exists;
        }

        public boolean exists() {
            return exists;
        }
    }
    
    public static class MockCurrency {
        private String name;
        private String title;
        
        public MockCurrency(String name, String title) {
            this.name = name;
            this.title = title;
        }
        
        public String getName() {
            return name;
        }
        
        public String getTitle() {
            return title;
        }
    }
    
    public static class MockUser {
        private String login;
        private String name;
        
        public MockUser(String login, String name) {
            this.login = login;
            this.name = name;
        }
        
        public String getLogin() {
            return login;
        }
        
        public String getName() {
            return name;
        }
    }
} 
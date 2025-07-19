package com.cherniva.frontui.controller;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.SessionValidationDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.frontui.service.AddAccountService;
import com.cherniva.frontui.service.DeleteUserService;
import com.cherniva.frontui.service.EditPasswordService;
import com.cherniva.frontui.service.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final SessionService sessionService;
    private final EditPasswordService editPasswordService;
    private final DeleteUserService deleteUserService;
    private final AddAccountService addAccountService;

    @GetMapping({"/", "/main"})
    public String mainPage(@CookieValue(value = "sessionId", required = false) String sessionId, Model model) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (sessionValidation.isValid()) {
                populateModelWithUserData(model, sessionValidation);
                return "main";
            }
        }

        // User is not authenticated
        model.addAttribute("authenticated", false);
        return "redirect:/login";
    }

    @PostMapping("/user/editPassword")
    public String editPassword(@CookieValue(value = "sessionId", required = false) String sessionId,
                               @RequestParam String password, @RequestParam String confirmPassword, Model model) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (!password.equals(confirmPassword)) {
                populateModelWithUserData(model, sessionValidation);
                model.addAttribute("passwordErrors", List.of("Passwords do not match"));
                return "main";
            }

            if (password == null || password.length() < 6) {
                populateModelWithUserData(model, sessionValidation);
                model.addAttribute("passwordErrors", List.of("Password must be at least 6 characters long"));
                return "main";
            }

            if (sessionValidation.isValid()) {
                UserAccountResponseDto userAccountResponseDto = editPasswordService.editPassword(sessionId, password);
                log.info("Updated user: {}", userAccountResponseDto);
                sessionService.logout(sessionId);
            }
        }
        model.addAttribute("authenticated", false);
        return "redirect:/login";
    }

    @PostMapping("/user/deleteAccount")
    public String deleteAccount(@CookieValue(value = "sessionId", required = false) String sessionId, Model model) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (sessionValidation.isValid()) {
                var accounts = sessionValidation.getAccounts();
                if (accounts != null && !accounts.isEmpty()) {
                    for (var account : accounts) {
                        if (account.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                            populateModelWithUserData(model, sessionValidation);
                            model.addAttribute("deletionErrors", List.of("Невозможно удалить аккаунт с ненулевыми счетами"));
                            return "main";
                        }
                    }
                }
                UserAccountResponseDto userAccountResponseDto = sessionValidation.getUserData();
                deleteUserService.deleteUser(sessionId);
                log.info("Delete user: {}", userAccountResponseDto);
            }
        }
        model.addAttribute("authenticated", false);
        return "redirect:/login";
    }

    @PostMapping("/user/addAccount")
    public String addAccount(@CookieValue(value = "sessionId", required = false) String sessionId,
                            @RequestParam String currencyCode, Model model, HttpServletResponse response) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (sessionValidation.isValid()) {
                log.info("Adding account for user: {} with currency: {}", sessionValidation.getUsername(), currencyCode);
                
                // Validate currency code is not empty
                if (currencyCode == null || currencyCode.trim().isEmpty()) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("addAccountErrors", List.of("Пожалуйста, выберите валюту"));
                    return "main";
                }
                
                // Check if user already has an account with this currency
                List<AccountDto> existingAccounts = sessionValidation.getAccounts();
                if (existingAccounts != null) {
                    boolean hasAccountWithCurrency = existingAccounts.stream()
                            .anyMatch(account -> currencyCode.equals(account.getCurrencyCode()));
                    
                    if (hasAccountWithCurrency) {
                        populateModelWithUserData(model, sessionValidation);
                        model.addAttribute("addAccountErrors", List.of("У вас уже есть счет в валюте " + currencyCode));
                        return "main";
                    }
                }

                var userResponse = addAccountService.addAccount(sessionId, currencyCode);
                log.info("Updated user: {}", userResponse);
                Cookie sessionCookie = new Cookie("sessionId", userResponse.getSessionId());
                sessionCookie.setPath("/");
                sessionCookie.setMaxAge(30 * 60); // 30 minutes
                response.addCookie(sessionCookie);
                return "redirect:/";
            }
        }
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
        List<AccountDto> accounts = sessionValidation.getAccounts();
        accounts = accounts.subList(0, accounts.size()/2);
        model.addAttribute("accounts", Objects.requireNonNullElseGet(accounts, () -> new ArrayList<AccountDto>()));
        
        // Mock currencies for dropdowns (this could be fetched from a service)
        List<MockCurrency> currencies = new ArrayList<>();
        currencies.add(new MockCurrency("USD", "Доллар США"));
        currencies.add(new MockCurrency("RUB", "Российский рубль"));
        currencies.add(new MockCurrency("CYN", "Китайский юань"));
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
        model.addAttribute("addAccountErrors", null);
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
    
    public static class MockCurrency {
        private String code;
        private String name;
        
        public MockCurrency(String code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getName() {
            return name;
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
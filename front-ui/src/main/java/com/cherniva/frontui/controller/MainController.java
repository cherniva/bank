package com.cherniva.frontui.controller;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.SessionValidationDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.frontui.service.*;
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
    private final EditUserService editUserService;
    private final DeleteUserService deleteUserService;
    private final AddAccountService addAccountService;
    private final CashService cashService;
    private final TransferService transferService;
    private final DeleteAccountService deleteAccountService;

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

    @PostMapping("/user/editUser")
    public String editUser(@CookieValue(value = "sessionId", required = false) String sessionId,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) String surname,
                          @RequestParam(required = false) String birthdate,
                          Model model,
                          HttpServletResponse response) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (sessionValidation.isValid()) {
                // Validate that at least one field is provided
                if ((name == null || name.trim().isEmpty()) && 
                    (surname == null || surname.trim().isEmpty()) &&
                    (birthdate == null || birthdate.trim().isEmpty())) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("userAccountsErrors", List.of("Пожалуйста, заполните хотя бы одно поле"));
                    return "main";
                }

                // Validate name if provided
                if (name != null && !name.trim().isEmpty() && name.trim().length() < 2) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("userAccountsErrors", List.of("Имя должно содержать минимум 2 символа"));
                    return "main";
                }

                // Validate surname if provided
                if (surname != null && !surname.trim().isEmpty() && surname.trim().length() < 2) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("userAccountsErrors", List.of("Фамилия должна содержать минимум 2 символа"));
                    return "main";
                }

                // Validate birthdate if provided
                if (birthdate != null && !birthdate.trim().isEmpty()) {
                    try {
                        java.time.LocalDate birthDate = java.time.LocalDate.parse(birthdate);
                        java.time.LocalDate eighteenYearsAgo = java.time.LocalDate.now().minusYears(18);
                        
                        if (birthDate.isAfter(eighteenYearsAgo)) {
                            populateModelWithUserData(model, sessionValidation);
                            model.addAttribute("userAccountsErrors", List.of("Пользователь должен быть старше 18 лет"));
                            return "main";
                        }
                    } catch (Exception e) {
                        populateModelWithUserData(model, sessionValidation);
                        model.addAttribute("userAccountsErrors", List.of("Неверный формат даты"));
                        return "main";
                    }
                }

                try {
                    UserAccountResponseDto userResponse = editUserService.editUser(sessionId, name, surname, birthdate);
                    
                    if (userResponse != null) {
                        // Update session cookie
                        Cookie sessionCookie = new Cookie("sessionId", userResponse.getSessionId());
                        sessionCookie.setPath("/");
                        sessionCookie.setMaxAge(30 * 60); // 30 minutes
                        response.addCookie(sessionCookie);
                        
                        log.info("User updated successfully: {}", userResponse.getUsername());
                        return "redirect:/";
                    } else {
                        populateModelWithUserData(model, sessionValidation);
                        model.addAttribute("userAccountsErrors", List.of("Ошибка при обновлении данных пользователя"));
                        return "main";
                    }
                } catch (Exception e) {
                    log.error("User edit operation failed", e);
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("userAccountsErrors", List.of("Ошибка при обновлении данных: " + e.getMessage()));
                    return "main";
                }
            }
        }
        model.addAttribute("authenticated", false);
        return "redirect:/login";
    }

    @PostMapping("/user/deleteUser")
    public String deleteUser(@CookieValue(value = "sessionId", required = false) String sessionId, Model model) {
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

    @PostMapping("/user/cash")
    public String handleCashOperation(@CookieValue(value = "sessionId", required = false) String sessionId,
                                    @RequestParam String currency,
                                    @RequestParam BigDecimal value,
                                    @RequestParam String action,
                                    Model model,
                                    HttpServletResponse response) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (sessionValidation.isValid()) {
                // Validate currency is not empty
                if (currency == null || currency.trim().isEmpty()) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("cashErrors", List.of("Пожалуйста, выберите валюту"));
                    return "main";
                }

                // Validate amount is positive
                if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("cashErrors", List.of("Сумма должна быть больше нуля"));
                    return "main";
                }

                // Find account with the specified currency for this user
                List<AccountDto> accounts = sessionValidation.getAccounts();
                AccountDto targetAccount = null;
                if (accounts != null) {
                    targetAccount = accounts.stream()
                            .filter(account -> currency.equals(account.getCurrencyCode()))
                            .findFirst()
                            .orElse(null);
                }

                if (targetAccount == null) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("cashErrors", List.of("У вас нет счета в валюте " + currency));
                    return "main";
                }

                try {
                    UserAccountResponseDto userResponse;
                    if ("PUT".equals(action)) {
                        // Deposit operation
                        userResponse = cashService.deposit(sessionId, targetAccount.getAccountId(), value);
                        log.info("Deposited {} {} to account {}", value, currency, targetAccount.getAccountId());
                    } else if ("GET".equals(action)) {
                        // Withdraw operation
                        userResponse = cashService.withdraw(sessionId, targetAccount.getAccountId(), value);
                        log.info("Withdrew {} {} from account {}", value, currency, targetAccount.getAccountId());
                    } else {
                        populateModelWithUserData(model, sessionValidation);
                        model.addAttribute("cashErrors", List.of("Неизвестная операция"));
                        return "main";
                    }

                    if (userResponse != null) {
                        // Update session cookie
                        Cookie sessionCookie = new Cookie("sessionId", userResponse.getSessionId());
                        sessionCookie.setPath("/");
                        sessionCookie.setMaxAge(30 * 60); // 30 minutes
                        response.addCookie(sessionCookie);
                        return "redirect:/";
                    } else {
                        populateModelWithUserData(model, sessionValidation);
                        model.addAttribute("cashErrors", List.of("Подозрительная операция. Отклонино"));
                        return "main";
                    }
                } catch (Exception e) {
                    log.error("Cash operation failed", e);
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("cashErrors", List.of("Ошибка при выполнении операции: " + e.getMessage()));
                    return "main";
                }
            }
        }
        model.addAttribute("authenticated", false);
        return "redirect:/login";
    }

    @PostMapping("/user/transfer")
    public String handleTransfer(@CookieValue(value = "sessionId", required = false) String sessionId,
                                 @RequestParam String fromCurrency,
                                 @RequestParam String toCurrency,
                                 @RequestParam BigDecimal value,
                                 @RequestParam(required = false) String to_login,
                                 Model model,
                                 HttpServletResponse response) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (sessionValidation.isValid()) {
                // Validate currencies are not empty
                if (fromCurrency == null || fromCurrency.trim().isEmpty()) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("transferErrors", List.of("Пожалуйста, выберите валюту для списания"));
                    return "main";
                }

                if (toCurrency == null || toCurrency.trim().isEmpty()) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("transferErrors", List.of("Пожалуйста, выберите валюту для зачисления"));
                    return "main";
                }

                // Validate amount is positive
                if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("transferErrors", List.of("Сумма должна быть больше нуля"));
                    return "main";
                }

                // Determine target username (self-transfer or other user)
                String targetUsername;
                if (to_login == null || to_login.trim().isEmpty()) {
                    // Self-transfer - use current user's username
                    targetUsername = sessionValidation.getUsername();
                } else {
                    // Transfer to other user
                    targetUsername = to_login.trim();
                }

                // Find source account with the specified currency for this user
                List<AccountDto> accounts = sessionValidation.getAccounts();
                AccountDto sourceAccount = null;
                if (accounts != null) {
                    sourceAccount = accounts.stream()
                            .filter(account -> fromCurrency.equals(account.getCurrencyCode()))
                            .findFirst()
                            .orElse(null);
                }

                if (sourceAccount == null) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("transferErrors", List.of("У вас нет счета в валюте " + fromCurrency));
                    return "main";
                }

                // Validate sufficient balance
                if (value.compareTo(sourceAccount.getAmount()) > 0) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("transferErrors", List.of("Недостаточно средств на счете"));
                    return "main";
                }

                try {
                    UserAccountResponseDto userResponse = transferService.transfer(
                            sessionId, value, fromCurrency, toCurrency, targetUsername);

                    if (userResponse != null) {
                        // Update session cookie
                        Cookie sessionCookie = new Cookie("sessionId", userResponse.getSessionId());
                        sessionCookie.setPath("/");
                        sessionCookie.setMaxAge(30 * 60); // 30 minutes
                        response.addCookie(sessionCookie);

                        log.info("Transfer successful: {} {} from {} to {} for user {}",
                                value, fromCurrency, sessionValidation.getUsername(), targetUsername, toCurrency);
                        return "redirect:/";
                    } else {
                        populateModelWithUserData(model, sessionValidation);
                        model.addAttribute("transferErrors", List.of("Подозрительная операция. Отклонино"));
                        return "main";
                    }
                } catch (Exception e) {
                    log.error("Transfer operation failed", e);
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("transferErrors", List.of("Ошибка при выполнении перевода: " + e.getMessage()));
                    return "main";
                }
            }
        }
        model.addAttribute("authenticated", false);
        return "redirect:/login";
    }

    @PostMapping("/user/deleteUserAccount")
    public String deleteUserAccount(@CookieValue(value = "sessionId", required = false) String sessionId,
                                   @RequestParam Long accountId,
                                   Model model,
                                   HttpServletResponse response) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);

            if (sessionValidation.isValid()) {
                // Validate that the account belongs to the current user
                List<AccountDto> userAccounts = sessionValidation.getAccounts();
                boolean accountBelongsToUser = userAccounts != null && userAccounts.stream()
                        .anyMatch(account -> account.getAccountId().equals(accountId));

                if (!accountBelongsToUser) {
                    log.error("User {} attempted to delete account {} that doesn't belong to them", 
                            sessionValidation.getUsername(), accountId);
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("deleteAccountErrors", List.of("Счет не найден или не принадлежит вам"));
                    return "main";
                }

                // Find the account to check if it has zero balance
                AccountDto accountToDelete = userAccounts.stream()
                        .filter(account -> account.getAccountId().equals(accountId))
                        .findFirst()
                        .orElse(null);

                if (accountToDelete != null && accountToDelete.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("deleteAccountErrors", List.of("Невозможно удалить счет с ненулевым балансом. Сначала переведите средства"));
                    return "main";
                }

                try {
                    // Call the accounts service to delete the account
                    // We'll need to create this service method
                    UserAccountResponseDto userResponse = deleteAccountService.deleteAccount(sessionId, accountId);

                    if (userResponse != null) {
                        // Update session cookie
                        Cookie sessionCookie = new Cookie("sessionId", userResponse.getSessionId());
                        sessionCookie.setPath("/");
                        sessionCookie.setMaxAge(30 * 60); // 30 minutes
                        response.addCookie(sessionCookie);

                        log.info("Account {} deleted successfully for user {}", accountId, sessionValidation.getUsername());
                        return "redirect:/";
                    } else {
                        populateModelWithUserData(model, sessionValidation);
                        model.addAttribute("deleteAccountErrors", List.of("Ошибка при удалении счета"));
                        return "main";
                    }
                } catch (Exception e) {
                    log.error("Account deletion failed", e);
                    populateModelWithUserData(model, sessionValidation);
                    model.addAttribute("deleteAccountErrors", List.of("Ошибка при удалении счета: " + e.getMessage()));
                    return "main";
                }
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
        
        // Real user data from session - separate name and surname
        model.addAttribute("name", sessionValidation.getName() != null ? sessionValidation.getName() : "");
        model.addAttribute("surname", sessionValidation.getSurname() != null ? sessionValidation.getSurname() : "");
        model.addAttribute("fullName", getFullName(sessionValidation));
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
        model.addAttribute("deleteAccountErrors", null);
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
package com.cherniva.accountsservice.contracts;

import com.cherniva.accountsservice.AccountsServiceApplication;
import com.cherniva.accountsservice.service.AuthService;
import com.cherniva.accountsservice.service.SessionService;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.AccountRepo;
import com.cherniva.common.repo.CurrencyRepo;
import com.cherniva.common.repo.UserDetailsRepo;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AccountsServiceApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
public abstract class BaseContractTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserDetailsRepo userDetailsRepo;

    @MockBean
    private AccountRepo accountRepo;

    @MockBean
    private CurrencyRepo currencyRepo;

    @MockBean
    private AuthService authService;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.webAppContextSetup(context);
        setupMockData();
    }

    private void setupMockData() {
        // Mock currencies
        Currency usd = new Currency();
        usd.setId(1L);
        usd.setCode("USD");
        usd.setName("US Dollar");

        Currency eur = new Currency();
        eur.setId(2L);
        eur.setCode("EUR");
        eur.setName("Euro");

        // Mock user
        UserDetails testUser = new UserDetails();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encoded_password");
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setBirthdate(LocalDate.of(1990, 1, 1));

        // Mock account
        Account account = new Account();
        account.setId(1L);
        account.setUserDetails(testUser);
        account.setCurrency(usd);
        account.setAmount(new BigDecimal("1000.00"));
        account.setActive(true);

        // Mock UserAccountResponseDto
        UserAccountResponseDto userResponseDto = new UserAccountResponseDto();
        userResponseDto.setUserId(1L);
        userResponseDto.setUsername("testuser");
        userResponseDto.setName("John");
        userResponseDto.setSurname("Doe");
        userResponseDto.setSessionId("test-session-123");

        // Mock session info
        SessionService.SessionInfo sessionInfo = new SessionService.SessionInfo(userResponseDto, System.currentTimeMillis());

        // Setup mock behaviors
        when(authService.authenticateUser(any(UserLoginDto.class))).thenReturn(userResponseDto);
        when(sessionService.getSession("test-session-123")).thenReturn(sessionInfo);
        when(sessionService.getSession("invalid-session")).thenReturn(null);
        when(sessionService.createSession(any(UserAccountResponseDto.class))).thenReturn("test-session-123");
        when(currencyRepo.findCurrencyByCode("USD")).thenReturn(java.util.Optional.of(usd));
        when(currencyRepo.findCurrencyByCode("EUR")).thenReturn(java.util.Optional.of(eur));
        when(userDetailsRepo.findById(1L)).thenReturn(java.util.Optional.of(testUser));
        when(accountRepo.findById(1L)).thenReturn(java.util.Optional.of(account));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    }
} 
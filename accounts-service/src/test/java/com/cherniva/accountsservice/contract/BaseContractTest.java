package com.cherniva.accountsservice.contract;

import com.cherniva.accountsservice.service.SessionService;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.repo.AccountRepo;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class})
public abstract class BaseContractTest {

    @Autowired
    private WebApplicationContext context;
    
    @MockBean
    private SessionService sessionService;
    
    @MockBean
    private AccountRepo accountRepo;

    @BeforeEach
    public void setup() {
        // Configure RestAssuredMockMvc with the web application context
        io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup(context);
        
        // Mock SessionService for test sessions
        UserAccountResponseDto mockUserData = new UserAccountResponseDto();
        mockUserData.setUserId(1L);
        mockUserData.setUsername("testuser");
        mockUserData.setName("Test");
        mockUserData.setSurname("User");
        mockUserData.setBirthdate(LocalDate.of(1990, 1, 1));
        mockUserData.setAccounts(new ArrayList<>());
        
        SessionService.SessionInfo mockSessionInfo = new SessionService.SessionInfo(mockUserData, System.currentTimeMillis());
        
        Mockito.when(sessionService.getSession("test-session-123")).thenReturn(mockSessionInfo);
        Mockito.when(sessionService.createSession(Mockito.any(UserAccountResponseDto.class))).thenReturn("new-session-id");
        
        // Mock AccountRepo for bank account operations
        Currency usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        
        Account validAccount1 = new Account();
        validAccount1.setId(1001L);
        validAccount1.setAmount(BigDecimal.valueOf(1000.00));
        validAccount1.setCurrency(usdCurrency);
        validAccount1.setActive(true);
        
        Account validAccount2 = new Account();
        validAccount2.setId(1002L);
        validAccount2.setAmount(BigDecimal.valueOf(500.00));
        validAccount2.setCurrency(usdCurrency);
        validAccount2.setActive(true);
        
        // Mock successful account lookups
        Mockito.when(accountRepo.findById(1001L)).thenReturn(Optional.of(validAccount1));
        Mockito.when(accountRepo.findById(1002L)).thenReturn(Optional.of(validAccount2));
        
        // Mock failed account lookups for error cases
        Mockito.when(accountRepo.findById(99999L)).thenReturn(Optional.empty());
        
        // Mock save operations
        Mockito.when(accountRepo.save(Mockito.any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
}

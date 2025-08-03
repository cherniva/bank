package com.cherniva.common.repo;

import com.cherniva.common.config.CommonTestConfiguration;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.model.ExchangeRate;
import com.cherniva.common.model.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = CommonTestConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.yml")
public abstract class BaseRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected AccountRepo accountRepo;

    @Autowired
    protected UserDetailsRepo userDetailsRepo;

    @Autowired
    protected CurrencyRepo currencyRepo;

    @Autowired
    protected ExchangeRateRepo exchangeRateRepo;

    protected Currency usdCurrency;
    protected Currency eurCurrency;
    protected Currency rubCurrency;
    protected UserDetails testUser1;
    protected UserDetails testUser2;
    protected Account usdAccount;
    protected Account eurAccount;
    protected ExchangeRate usdEurRate;

    @BeforeEach
    void setupTestData() {
        // Create currencies
        usdCurrency = new Currency();
        usdCurrency.setId(1L);
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        usdCurrency = entityManager.persistAndFlush(usdCurrency);

        eurCurrency = new Currency();
        eurCurrency.setId(2L);
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");
        eurCurrency = entityManager.persistAndFlush(eurCurrency);

        rubCurrency = new Currency();
        rubCurrency.setId(3L);
        rubCurrency.setCode("RUB");
        rubCurrency.setName("Russian Ruble");
        rubCurrency = entityManager.persistAndFlush(rubCurrency);

        // Create users
        testUser1 = new UserDetails();
        testUser1.setId(1L);
        testUser1.setUsername("testuser1");
        testUser1.setPassword("encodedPassword1");
        testUser1.setName("John");
        testUser1.setSurname("Doe");
        testUser1.setBirthdate(LocalDate.of(1990, 1, 1));
        testUser1 = entityManager.persistAndFlush(testUser1);

        testUser2 = new UserDetails();
        testUser2.setId(2L);
        testUser2.setUsername("testuser2");
        testUser2.setPassword("encodedPassword2");
        testUser2.setName("Jane");
        testUser2.setSurname("Smith");
        testUser2.setBirthdate(LocalDate.of(1992, 5, 15));
        testUser2 = entityManager.persistAndFlush(testUser2);

        // Create accounts
        usdAccount = new Account();
        usdAccount.setId(1L);
        usdAccount.setUserDetails(testUser1);
        usdAccount.setCurrency(usdCurrency);
        usdAccount.setAmount(new BigDecimal("1000.00"));
        usdAccount.setActive(true);
        usdAccount = entityManager.persistAndFlush(usdAccount);

        eurAccount = new Account();
        eurAccount.setId(2L);
        eurAccount.setUserDetails(testUser1);
        eurAccount.setCurrency(eurCurrency);
        eurAccount.setAmount(new BigDecimal("500.00"));
        eurAccount.setActive(true);
        eurAccount = entityManager.persistAndFlush(eurAccount);

        // Create exchange rate
        usdEurRate = new ExchangeRate();
        usdEurRate.setSourceCurrency(usdCurrency);
        usdEurRate.setTargetCurrency(eurCurrency);
        usdEurRate.setBuyPrice(new BigDecimal("0.85"));
        usdEurRate.setSellPrice(new BigDecimal("0.82"));
        usdEurRate = entityManager.persistAndFlush(usdEurRate);

        entityManager.clear();
    }
} 
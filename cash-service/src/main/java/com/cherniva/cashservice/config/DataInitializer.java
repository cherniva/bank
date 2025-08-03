package com.cherniva.cashservice.config;

import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.AccountRepo;
import com.cherniva.common.repo.CurrencyRepo;
import com.cherniva.common.repo.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CurrencyRepo currencyRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final AccountRepo accountRepo;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Starting data initialization...");
            
            // Create currencies
            log.info("Creating currencies...");
            Currency rub = new Currency();
            rub.setId(1L);
            rub.setCode("RUB");
            rub.setName("Russian ruble");
            currencyRepo.save(rub);
            log.info("Created RUB currency");

            Currency usd = new Currency();
            usd.setId(2L);
            usd.setCode("USD");
            usd.setName("US Dollar");
            currencyRepo.save(usd);
            log.info("Created USD currency");

            Currency yen = new Currency();
            yen.setId(3L);
            yen.setCode("CNY");
            yen.setName("Chinese yen");
            currencyRepo.save(yen);
            log.info("Created CNY currency");

            // Create users
            log.info("Creating users...");
            UserDetails user1 = new UserDetails();
            user1.setId(1L);
            user1.setUsername("testuser");
            user1.setPassword("password123");
            user1.setName("Test");
            user1.setSurname("User");
            user1.setBirthdate(LocalDate.of(1990, 1, 1));
            userDetailsRepo.save(user1);
            log.info("Created user 1");

            UserDetails user2 = new UserDetails();
            user2.setId(2L);
            user2.setUsername("testuser2");
            user2.setPassword("password123");
            user2.setName("Test");
            user2.setSurname("User2");
            user2.setBirthdate(LocalDate.of(1995, 5, 15));
            userDetailsRepo.save(user2);
            log.info("Created user 2");

            // Create accounts
            log.info("Creating accounts...");
            Account account1 = new Account();
            account1.setId(1001L);
            account1.setUserDetails(user1);
            account1.setCurrency(usd);
            account1.setAmount(BigDecimal.valueOf(1000.00));
            account1.setActive(true);
            accountRepo.save(account1);
            log.info("Created account 1001");

            Account account2 = new Account();
            account2.setId(1002L);
            account2.setUserDetails(user2);
            account2.setCurrency(yen);
            account2.setAmount(BigDecimal.valueOf(500.00));
            account2.setActive(true);
            accountRepo.save(account2);
            log.info("Created account 1002");

            log.info("Data initialization completed successfully!");
        } catch (Exception e) {
            log.error("Error during data initialization", e);
            throw e;
        }
    }
} 
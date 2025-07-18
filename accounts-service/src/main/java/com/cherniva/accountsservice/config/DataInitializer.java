package com.cherniva.accountsservice.config;

import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.UserDetailsRepo;
import com.cherniva.common.repo.CurrencyRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserDetailsRepo userDetailsRepo;
    private final CurrencyRepo currencyRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Starting data initialization...");
            
            // Create currencies
            log.info("Creating currencies...");
            Currency rub = new Currency();
            rub.setCode("RUB");
            rub.setName("Russian ruble");
            currencyRepo.save(rub);
            log.info("Created RUB currency");

            Currency usd = new Currency();
            usd.setCode("USD");
            usd.setName("US Dollar");
            currencyRepo.save(usd);
            log.info("Created USD currency");

            Currency yen = new Currency();
            yen.setCode("CYN");
            yen.setName("Chinese yen");
            currencyRepo.save(yen);
            log.info("Created CYN currency");

            // Create user
            log.info("Creating user...");
            UserDetails user = new UserDetails();
            user.setUsername("1");
            user.setPassword(passwordEncoder.encode("1"));
            user.setName("John");
            user.setSurname("Doe");
            user.setBirthdate(LocalDate.of(1990, 12, 13));

            // Create accounts
            log.info("Creating accounts...");
            Account account1 = new Account();
            account1.setCurrency(usd);
            account1.setUserDetails(user);
            account1.setAmount(BigDecimal.valueOf(100L));
            account1.setActive(true);

            Account account2 = new Account();
            account2.setCurrency(yen);
            account2.setUserDetails(user);
            account2.setAmount(BigDecimal.valueOf(100L));
            account2.setActive(true);

            user.setAccounts(Arrays.asList(account1, account2));
            userDetailsRepo.save(user);
            log.info("Created user with accounts");

            System.out.println("Sample data initialized!");
            System.out.println("Username: 1, Password: 1");
            log.info("Data initialization completed successfully!");
        } catch (Exception e) {
            log.error("Error during data initialization", e);
            throw e;
        }
    }
} 
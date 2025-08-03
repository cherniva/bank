package com.cherniva.accountsservice.config;

import com.cherniva.accountsservice.service.SyncService;
import com.cherniva.accountsservice.utils.SeqGenerator;
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
    private final SyncService syncService;

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

            // Create user
            log.info("Creating user...");
            UserDetails user = new UserDetails();
            user.setId(SeqGenerator.getNextUserDetails());
            user.setUsername("1");
            user.setPassword(passwordEncoder.encode("1"));
            user.setName("John");
            user.setSurname("Doe");
            user.setBirthdate(LocalDate.of(1990, 12, 13));

            UserDetails user1 = new UserDetails();
            user1.setId(SeqGenerator.getNextUserDetails());
            user1.setUsername("ch");
            user1.setPassword(passwordEncoder.encode("ch"));
            user1.setName("Ivan");
            user1.setSurname("Chernikov");
            user1.setBirthdate(LocalDate.of(2000, 6, 13));

            // Create accounts
            log.info("Creating accounts...");
            Account account1 = new Account();
            account1.setId(SeqGenerator.getNextAccount());
            account1.setCurrency(usd);
            account1.setUserDetails(user);
            account1.setAmount(BigDecimal.valueOf(100L));
            account1.setActive(true);

            Account account2 = new Account();
            account2.setId(SeqGenerator.getNextAccount());
            account2.setCurrency(yen);
            account2.setUserDetails(user);
            account2.setAmount(BigDecimal.valueOf(100L));
            account2.setActive(true);

            Account account3 = new Account();
            account3.setId(SeqGenerator.getNextAccount());
            account3.setCurrency(rub);
            account3.setUserDetails(user);
            account3.setAmount(BigDecimal.valueOf(100L));
            account3.setActive(true);

            user.setAccounts(Arrays.asList(account1, account2, account3));
            userDetailsRepo.save(user);
            syncService.syncUserCreation(user);
//            for (var account : user.getAccounts()) {
//                syncService.syncCreation(account);
//            }
            log.info("Created user with accounts");
            userDetailsRepo.save(user1);
            syncService.syncUserCreation(user1);

            System.out.println("Sample data initialized!");
            System.out.println("Username: 1, Password: 1");
            System.out.println("Username: ch, Password: ch");
            log.info("Data initialization completed successfully!");
        } catch (Exception e) {
            log.error("Error during data initialization", e);
            throw e;
        }
    }
} 
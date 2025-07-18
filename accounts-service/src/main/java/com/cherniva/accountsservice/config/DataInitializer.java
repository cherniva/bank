package com.cherniva.accountsservice.config;

import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.UserDetailsRepo;
import com.cherniva.common.repo.CurrencyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserDetailsRepo userDetailsRepo;
    private final CurrencyRepo currencyRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create currencies
        Currency usd = new Currency();
        usd.setCode("USD");
        usd.setName("US Dollar");
        currencyRepo.save(usd);

        Currency eur = new Currency();
        eur.setCode("EUR");
        eur.setName("Euro");
        currencyRepo.save(eur);

        // Create user
        UserDetails user = new UserDetails();
        user.setUsername("1");
        user.setPassword(passwordEncoder.encode("1"));
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthdate(LocalDate.of(1990, 12, 13));

        // Create accounts
        Account account1 = new Account();
        account1.setCurrency(usd);
        account1.setUserDetails(user);

        Account account2 = new Account();
        account2.setCurrency(eur);
        account2.setUserDetails(user);

        user.setAccounts(Arrays.asList(account1, account2));
        userDetailsRepo.save(user);

        System.out.println("Sample data initialized!");
        System.out.println("Username: 1, Password: 1");
    }
} 
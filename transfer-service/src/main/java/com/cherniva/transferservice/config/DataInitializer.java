package com.cherniva.transferservice.config;

import com.cherniva.common.model.Currency;
import com.cherniva.common.repo.CurrencyRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CurrencyRepo currencyRepo;

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
            log.info("Data initialization completed successfully!");
        } catch (Exception e) {
            log.error("Error during data initialization", e);
            throw e;
        }
    }
} 
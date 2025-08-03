package com.cherniva.transferservice.service;

import com.cherniva.common.dto.ExchangeRateDto;
import com.cherniva.common.dto.ExchangeRatesResponseDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.hibernate.engine.jdbc.Size.DEFAULT_SCALE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;
    private final AccountRepo accountRepo;
    private final AccountService accountService;
    private final SyncService syncService;
    private final SessionService sessionService;

    public UserAccountResponseDto transfer(String sessionId, Long accountId, BigDecimal amount, String fromCurrency,
                                           String toCurrency, String username) {
        try {
            var valid = validOperation();
            log.info("Operation validity status: {}", valid ? "valid" : "invalid");

            if (valid) {
                var rate = getExchangeRate(fromCurrency, toCurrency);
                var validatedRate = validateRate(rate, fromCurrency, toCurrency); // reverse if needed
                var sourceAccount = accountRepo.findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Source account with currency " + validatedRate.getFromCurrency() + " not found"));

                // Validate sufficient balance
                if (amount.compareTo(sourceAccount.getAmount()) > 0) {
                    throw new RuntimeException("Insufficient balance for transfer");
                }

                var destinationAccountDto = accountService.getAccountByUsernameAndCurrencyCode(username, validatedRate.getToCurrency());
                var destinationAccount = accountRepo.findById(destinationAccountDto.getAccountId()).get();

                // Calculate converted amount using exchange rate
                BigDecimal convertedAmount = amount.multiply(validatedRate.getSellRate());

                // Perform the transfer
                sourceAccount.setAmount(sourceAccount.getAmount().subtract(amount));
                destinationAccount.setAmount(destinationAccount.getAmount().add(convertedAmount));

                // Save both accounts
                accountRepo.save(sourceAccount);
                accountRepo.save(destinationAccount);

                log.info("Successful transfer of {} {} to {} {} (converted amount: {} {})",
                        amount, validatedRate.getFromCurrency(),
                        username, validatedRate.getToCurrency(),
                        convertedAmount, validatedRate.getToCurrency());

                syncService.syncTransfer(sourceAccount, destinationAccount);

                var updatedUserAccountResponseDto = sessionService.updateSession(sessionId);

                // Send notification for successful transfer
                notificationService.sendTransferNotification(
                        updatedUserAccountResponseDto.getUserId().toString(),
                        updatedUserAccountResponseDto.getUsername(),
                        amount,
                        fromCurrency,
                        toCurrency,
                        username,
                        convertedAmount,
                        true // success
                );

                return updatedUserAccountResponseDto;
            } else {
                // Send notification for failed operation (blocked by fraud detection)
                notificationService.sendTransferNotification(
                        null, // userId will be null for failed operations
                        null, // username will be null for failed operations
                        amount,
                        fromCurrency,
                        toCurrency,
                        username,
                        null, // convertedAmount will be null for failed operations
                        false // failed
                );
                return null;
            }
        } catch (Exception e) {
            log.error("Transfer operation failed", e);

            // Send notification for failed operation (exception)
            notificationService.sendTransferNotification(
                    null, // userId will be null for failed operations
                    null, // username will be null for failed operations
                    amount,
                    fromCurrency,
                    toCurrency,
                    username,
                    null, // convertedAmount will be null for failed operations
                    false // failed
            );

            throw new RuntimeException(e);
        }
    }

    private ExchangeRateDto validateRate(ExchangeRateDto rate, String fromCurrency, String toCurrency) {
        if (rate.getFromCurrency().equals(fromCurrency) && rate.getToCurrency().equals(toCurrency)) {
            return rate;
        }
        ExchangeRateDto exchangeRateDto = new ExchangeRateDto();
        exchangeRateDto.setFromCurrency(fromCurrency);
        exchangeRateDto.setToCurrency(toCurrency);
        exchangeRateDto.setBuyRate(BigDecimal.ONE.divide(rate.getSellRate(), DEFAULT_SCALE, RoundingMode.DOWN));
        exchangeRateDto.setSellRate(BigDecimal.ONE.divide(rate.getBuyRate(), DEFAULT_SCALE, RoundingMode.DOWN));
        return exchangeRateDto;
    }

    private ExchangeRateDto getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            ExchangeRateDto exchangeRateDto = new ExchangeRateDto();
            exchangeRateDto.setFromCurrency(fromCurrency);
            exchangeRateDto.setToCurrency(toCurrency);
            exchangeRateDto.setBuyRate(BigDecimal.ONE);
            exchangeRateDto.setSellRate(BigDecimal.ONE);
            return exchangeRateDto;
        }
        var rates = getExchangeRates();
        return rates.getRates().stream()
                .filter(rate ->
                        (rate.getFromCurrency().equals(fromCurrency) && rate.getToCurrency().equals(toCurrency)) ||
                                (rate.getFromCurrency().equals(toCurrency) && rate.getToCurrency().equals(fromCurrency)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Exchange rate not found for " + fromCurrency + " to " + toCurrency));
    }

    private ExchangeRatesResponseDto getExchangeRates() {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/exchange/course");

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.GET,
                            requestEntity,
                            ExchangeRatesResponseDto.class)
                    .getBody();
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    private Boolean validOperation() {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/blocker/check");

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.GET,
                            requestEntity,
                            Boolean.class)
                    .getBody();
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }
}

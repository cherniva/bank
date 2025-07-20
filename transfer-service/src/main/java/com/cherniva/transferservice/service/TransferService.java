package com.cherniva.transferservice.service;

import com.cherniva.common.dto.ExchangeRateDto;
import com.cherniva.common.dto.ExchangeRatesResponseDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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

    public UserAccountResponseDto transfer(String sessionId, BigDecimal amount, String fromCurrency,
                                           String toCurrency, String username) {
        try {
            var valid = validOperation();
            log.info("Operation validity status: {}", valid ? "valid" : "invalid");
            if (valid) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/accounts/transfer")
                        .queryParam("sessionId", sessionId)
                        .queryParam("amount", amount)
                        .queryParam("username", username);

                var rate = getExchangeRate(fromCurrency, toCurrency);
                rate = validateRate(rate, fromCurrency, toCurrency); // reverse if needed
                System.out.println(rate);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<ExchangeRateDto> requestEntity = new HttpEntity<>(rate, headers);

                return restTemplate.exchange(
                                builder.toUriString(),
                                HttpMethod.POST,
                                requestEntity,
                                UserAccountResponseDto.class)
                        .getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("", e);
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

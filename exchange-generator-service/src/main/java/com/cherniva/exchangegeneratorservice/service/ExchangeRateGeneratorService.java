package com.cherniva.exchangegeneratorservice.service;

import com.cherniva.common.dto.ExchangeRateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class ExchangeRateGeneratorService {
    
    private final RestTemplate restTemplate;
    private final Random random = new Random();
    
    // Base rates for RUB-USD and RUB-CNY
    private BigDecimal rubUsdBuyRate = new BigDecimal("0.0112");
    private BigDecimal rubUsdSellRate = new BigDecimal("0.0108");
    private BigDecimal rubCnyBuyRate = new BigDecimal("0.0815");
    private BigDecimal rubCnySellRate = new BigDecimal("0.0792");
    
    @Autowired
    public ExchangeRateGeneratorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void generateAndSendExchangeRates() {
        try {
            List<ExchangeRateDto> rates = new ArrayList<>();
            
            // Generate RUB-USD rates
            ExchangeRateDto rubUsd = generateRubUsdRate();
            rates.add(rubUsd);
            
            // Generate RUB-CNY rates
            ExchangeRateDto rubCny = generateRubCnyRate();
            rates.add(rubCny);
            
            // Create headers with JWT token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            // Create HTTP entity with headers and body
            HttpEntity<List<ExchangeRateDto>> requestEntity = new HttpEntity<>(rates, headers);
            
            // Send to exchange service with token
            String exchangeServiceUrl = "lb://api-gateway/exchange/course/update";
            ResponseEntity<Object> response = restTemplate.exchange(
                exchangeServiceUrl, 
                HttpMethod.POST, 
                requestEntity, 
                Object.class
            );
            
            log.info("Generated and sent new exchange rates at: {}", LocalDateTime.now());
            log.info("Response status: {}", response.getStatusCode());
            
        } catch (Exception e) {
            log.error("Error generating/sending exchange rates", e);
        }
    }
    
    private ExchangeRateDto generateRubUsdRate() {
        // Generate small random variations (±2%)
        double buyVariation = 1.0 + (random.nextDouble() - 0.5) * 0.04; // ±2%
        double sellVariation = 1.0 + (random.nextDouble() - 0.5) * 0.04; // ±2%
        
        BigDecimal newBuyRate = rubUsdBuyRate.multiply(BigDecimal.valueOf(buyVariation))
                .setScale(6, RoundingMode.HALF_UP);
        BigDecimal newSellRate = rubUsdSellRate.multiply(BigDecimal.valueOf(sellVariation))
                .setScale(6, RoundingMode.HALF_UP);
        
        // Ensure sell rate is always lower than buy rate (spread)
        if (newSellRate.compareTo(newBuyRate) >= 0) {
            newSellRate = newBuyRate.multiply(new BigDecimal("0.98"));
        }
        
        // Update base rates
        rubUsdBuyRate = newBuyRate;
        rubUsdSellRate = newSellRate;
        
        ExchangeRateDto rate = new ExchangeRateDto();
        rate.setFromCurrency("RUB");
        rate.setToCurrency("USD");
        rate.setBuyRate(newBuyRate);
        rate.setSellRate(newSellRate);
        rate.setLastUpdated(LocalDateTime.now());
        
        return rate;
    }
    
    private ExchangeRateDto generateRubCnyRate() {
        // Generate small random variations (±2%)
        double buyVariation = 1.0 + (random.nextDouble() - 0.5) * 0.04; // ±2%
        double sellVariation = 1.0 + (random.nextDouble() - 0.5) * 0.04; // ±2%
        
        BigDecimal newBuyRate = rubCnyBuyRate.multiply(BigDecimal.valueOf(buyVariation))
                .setScale(6, RoundingMode.HALF_UP);
        BigDecimal newSellRate = rubCnySellRate.multiply(BigDecimal.valueOf(sellVariation))
                .setScale(6, RoundingMode.HALF_UP);
        
        // Ensure sell rate is always lower than buy rate (spread)
        if (newSellRate.compareTo(newBuyRate) >= 0) {
            newSellRate = newBuyRate.multiply(new BigDecimal("0.98"));
        }
        
        // Update base rates
        rubCnyBuyRate = newBuyRate;
        rubCnySellRate = newSellRate;
        
        ExchangeRateDto rate = new ExchangeRateDto();
        rate.setFromCurrency("RUB");
        rate.setToCurrency("CNY");
        rate.setBuyRate(newBuyRate);
        rate.setSellRate(newSellRate);
        rate.setLastUpdated(LocalDateTime.now());
        
        return rate;
    }
} 
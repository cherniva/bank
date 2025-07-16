package com.cherniva.exchangegeneratorservice.service;

import com.cherniva.common.dto.ExchangeRateDto;
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
public class ExchangeRateGeneratorService {
    
    private final RestTemplate restTemplate;
    private final Random random = new Random();
    
    // Test JWT token
    private static final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJWY1JQOERKTEFiTU5fV29POG5EZmktWmlCVVpjVlNWRjUwNkxGVXdUci0wIn0.eyJleHAiOjE3NTI2OTE4MTIsImlhdCI6MTc1MjY5MDAxMiwianRpIjoiYzZhZjAyMWQtYzA3ZS00NTdkLThhMjUtMjg1Yzc5MWE0YTFhIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9iYW5rcmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYTk3ZGZjMzMtYTYyOC00NzBkLTllOWUtZThhYzBmMmI3Y2JiIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZ2F0ZXdheSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiLyoiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwiZGVmYXVsdC1yb2xlcy1iYW5rcmVhbG0iLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCByZXNvdXJjZS5yZWFkIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJjbGllbnRIb3N0IjoiMTkyLjE2OC42NS4xIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LWdhdGV3YXkiLCJjbGllbnRBZGRyZXNzIjoiMTkyLjE2OC42NS4xIiwiY2xpZW50X2lkIjoiZ2F0ZXdheSJ9.FfdcAJkIEpr4uxp8j3gjPlud1nzyJjufzqf4ryBxFqe_SBObG9xDfA3H_IMrMdW2KHCPCRJYz7miD2PxDuWhyC-zUsQl8wNo44F_uhnPd7Dm-8dAyCfAd0sC_s65cpCRNHXbOgtevd2gcHUmWOsP5a87XMrxDudbEERgNralrnxyM5-EHFZJmZuRyeNOpRCFgyka7Ty9J98F3P6j2KfmMhAsfBQrNpux_-iRBqbrQQkkyEsuJ37SggcW7jKmRAdM90LHWFXV2DN8NKQbDLb1a250rJXjbX0aCV4TXWmN3RtuXOtoH2DPKmwvgi5Pq8C3HbasuDtL3F7SO1GUx4d-RA";
    
    // Base rates for RUB-USD and RUB-CNY
    private BigDecimal rubUsdBuyRate = new BigDecimal("0.0112");
    private BigDecimal rubUsdSellRate = new BigDecimal("0.0108");
    private BigDecimal rubCnyBuyRate = new BigDecimal("0.0815");
    private BigDecimal rubCnySellRate = new BigDecimal("0.0792");
    
    @Autowired
    public ExchangeRateGeneratorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
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
            headers.set("Authorization", "Bearer " + JWT_TOKEN);
            headers.set("Content-Type", "application/json");
            
            // Create HTTP entity with headers and body
            HttpEntity<List<ExchangeRateDto>> requestEntity = new HttpEntity<>(rates, headers);
            
            // Send to exchange service with token
            String exchangeServiceUrl = "http://localhost:8093/exchange/course/update";
            ResponseEntity<Object> response = restTemplate.exchange(
                exchangeServiceUrl, 
                HttpMethod.POST, 
                requestEntity, 
                Object.class
            );
            
            System.out.println("Generated and sent new exchange rates at: " + LocalDateTime.now());
            System.out.println("Response status: " + response.getStatusCode());
            
        } catch (Exception e) {
            System.err.println("Error generating/sending exchange rates: " + e.getMessage());
            e.printStackTrace();
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
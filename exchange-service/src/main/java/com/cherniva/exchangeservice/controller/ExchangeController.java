package com.cherniva.exchangeservice.controller;

import com.cherniva.common.dto.ExchangeRateDto;
import com.cherniva.common.dto.ExchangeRatesResponseDto;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/exchange/course")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class ExchangeController {
    
    // Store the latest rates in memory
    private final ConcurrentMap<String, ExchangeRateDto> latestRates = new ConcurrentHashMap<>();
    
    @GetMapping
    public ExchangeRatesResponseDto getExchangeRates() {
        ExchangeRatesResponseDto response = new ExchangeRatesResponseDto();
        
        // Return stored rates if available, otherwise return default rates
        List<ExchangeRateDto> rates = new ArrayList<>();
        
        ExchangeRateDto rubUsd = latestRates.get("RUB-USD");
        if (rubUsd == null) {
            rubUsd = createDefaultRubUsdRate();
        }
        rates.add(rubUsd);
        
        ExchangeRateDto rubCny = latestRates.get("RUB-CNY");
        if (rubCny == null) {
            rubCny = createDefaultRubCnyRate();
        }
        rates.add(rubCny);
        
        // Add USD-CNY pair (USD -> RUB -> CNY)
        ExchangeRateDto usdCny = latestRates.get("USD-CNY");
        if (usdCny == null) {
            usdCny = calculateUsdCnyRate(rubUsd, rubCny);
        }
        rates.add(usdCny);
        
        // Add CNY-USD pair (CNY -> RUB -> USD)
//        ExchangeRateDto cnyUsd = latestRates.get("CNY-USD");
//        if (cnyUsd == null) {
//            cnyUsd = calculateCnyUsdRate(rubUsd, rubCny);
//        }
//        rates.add(cnyUsd);
        
        response.setRates(rates);
        response.setMessage("Exchange rates retrieved successfully");
        response.setSuccess(true);
        
        return response;
    }
    
    @PostMapping("/update")
    public ExchangeRatesResponseDto updateExchangeRates(@RequestBody List<ExchangeRateDto> newRates) {
        ExchangeRatesResponseDto response = new ExchangeRatesResponseDto();
        
        // Update stored rates
        for (ExchangeRateDto rate : newRates) {
            String key = rate.getFromCurrency() + "-" + rate.getToCurrency();
            latestRates.put(key, rate);
        }
        
        response.setRates(new ArrayList<>(latestRates.values()));
        response.setMessage("Exchange rates updated successfully");
        response.setSuccess(true);
        
        return response;
    }
    
    private ExchangeRateDto createDefaultRubUsdRate() {
        ExchangeRateDto rubUsd = new ExchangeRateDto();
        rubUsd.setFromCurrency("RUB");
        rubUsd.setToCurrency("USD");
        rubUsd.setBuyRate(new BigDecimal("0.0112"));
        rubUsd.setSellRate(new BigDecimal("0.0108"));
        rubUsd.setLastUpdated(LocalDateTime.now());
        return rubUsd;
    }
    
    private ExchangeRateDto createDefaultRubCnyRate() {
        ExchangeRateDto rubCny = new ExchangeRateDto();
        rubCny.setFromCurrency("RUB");
        rubCny.setToCurrency("CNY");
        rubCny.setBuyRate(new BigDecimal("0.0815"));
        rubCny.setSellRate(new BigDecimal("0.0792"));
        rubCny.setLastUpdated(LocalDateTime.now());
        return rubCny;
    }
    
    private ExchangeRateDto calculateUsdCnyRate(ExchangeRateDto rubUsd, ExchangeRateDto rubCny) {
        ExchangeRateDto usdCny = new ExchangeRateDto();
        usdCny.setFromCurrency("USD");
        usdCny.setToCurrency("CNY");
        // USD -> CNY calculation:
        // RUB-USD = 0.0112 means 1 RUB = 0.0112 USD, so 1 USD = 1/0.0112 = 89.29 RUB
        // RUB-CNY = 0.0815 means 1 RUB = 0.0815 CNY
        // Therefore: 1 USD = 89.29 RUB = 89.29 × 0.0815 = 7.28 CNY
        usdCny.setBuyRate(BigDecimal.ONE.divide(rubUsd.getBuyRate(), 4, BigDecimal.ROUND_HALF_EVEN).multiply(rubCny.getBuyRate()));
        usdCny.setSellRate(BigDecimal.ONE.divide(rubUsd.getSellRate(), 4, BigDecimal.ROUND_HALF_EVEN).multiply(rubCny.getSellRate()));
        usdCny.setLastUpdated(LocalDateTime.now());
        return usdCny;
    }
    
    private ExchangeRateDto calculateCnyUsdRate(ExchangeRateDto rubUsd, ExchangeRateDto rubCny) {
        ExchangeRateDto cnyUsd = new ExchangeRateDto();
        cnyUsd.setFromCurrency("CNY");
        cnyUsd.setToCurrency("USD");
        // CNY -> USD calculation:
        // RUB-CNY = 0.0815 means 1 RUB = 0.0815 CNY, so 1 CNY = 1/0.0815 = 12.27 RUB
        // RUB-USD = 0.0112 means 1 RUB = 0.0112 USD
        // Therefore: 1 CNY = 12.27 RUB = 12.27 × 0.0112 = 0.137 USD
        cnyUsd.setBuyRate(BigDecimal.ONE.divide(rubCny.getBuyRate(), 4, BigDecimal.ROUND_HALF_EVEN).multiply(rubUsd.getBuyRate()));
        cnyUsd.setSellRate(BigDecimal.ONE.divide(rubCny.getSellRate(), 4, BigDecimal.ROUND_HALF_EVEN).multiply(rubUsd.getSellRate()));
        cnyUsd.setLastUpdated(LocalDateTime.now());
        return cnyUsd;
    }
}

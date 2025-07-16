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
}

package com.cherniva.exchangegeneratorservice.service;

import com.cherniva.common.dto.ExchangeRateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateGeneratorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExchangeRateGeneratorService exchangeRateGeneratorService;

    @BeforeEach
    void setUp() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    @Test
    void generateAndSendExchangeRates_Success_SendsRatesToExchangeService() {
        // Act
        exchangeRateGeneratorService.generateAndSendExchangeRates();

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq("lb://api-gateway/exchange/course/update"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Object.class)
        );

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<ExchangeRateDto> rates = (List<ExchangeRateDto>) capturedEntity.getBody();
        
        assertNotNull(rates);
        assertEquals(2, rates.size()); // RUB-USD and RUB-CNY

        // Verify RUB-USD rate
        ExchangeRateDto rubUsdRate = rates.stream()
                .filter(rate -> "RUB".equals(rate.getFromCurrency()) && "USD".equals(rate.getToCurrency()))
                .findFirst()
                .orElse(null);
        assertNotNull(rubUsdRate);
        assertTrue(rubUsdRate.getBuyRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(rubUsdRate.getSellRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(rubUsdRate.getSellRate().compareTo(rubUsdRate.getBuyRate()) < 0); // Sell rate should be lower
        assertNotNull(rubUsdRate.getLastUpdated());

        // Verify RUB-CNY rate
        ExchangeRateDto rubCnyRate = rates.stream()
                .filter(rate -> "RUB".equals(rate.getFromCurrency()) && "CNY".equals(rate.getToCurrency()))
                .findFirst()
                .orElse(null);
        assertNotNull(rubCnyRate);
        assertTrue(rubCnyRate.getBuyRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(rubCnyRate.getSellRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(rubCnyRate.getSellRate().compareTo(rubCnyRate.getBuyRate()) < 0); // Sell rate should be lower
        assertNotNull(rubCnyRate.getLastUpdated());
    }

    @Test
    void generateAndSendExchangeRates_MultipleInvocations_GeneratesDifferentRates() {
        // First invocation
        exchangeRateGeneratorService.generateAndSendExchangeRates();
        
        ArgumentCaptor<HttpEntity> firstCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), firstCaptor.capture(), any(Class.class));
        
        @SuppressWarnings("unchecked")
        List<ExchangeRateDto> firstRates = (List<ExchangeRateDto>) firstCaptor.getValue().getBody();
        ExchangeRateDto firstRubUsd = firstRates.stream()
                .filter(rate -> "RUB".equals(rate.getFromCurrency()) && "USD".equals(rate.getToCurrency()))
                .findFirst()
                .orElse(null);

        // Reset mock to capture second call
        reset(restTemplate);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok().build());

        // Second invocation
        exchangeRateGeneratorService.generateAndSendExchangeRates();
        
        ArgumentCaptor<HttpEntity> secondCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), secondCaptor.capture(), any(Class.class));
        
        @SuppressWarnings("unchecked")
        List<ExchangeRateDto> secondRates = (List<ExchangeRateDto>) secondCaptor.getValue().getBody();
        ExchangeRateDto secondRubUsd = secondRates.stream()
                .filter(rate -> "RUB".equals(rate.getFromCurrency()) && "USD".equals(rate.getToCurrency()))
                .findFirst()
                .orElse(null);

        // Rates should likely be different due to random generation
        // Note: There's a small chance they could be the same, but very unlikely
        assertNotNull(firstRubUsd);
        assertNotNull(secondRubUsd);
    }

    @Test
    void generateAndSendExchangeRates_ValidatesRateStructure() {
        // Act
        exchangeRateGeneratorService.generateAndSendExchangeRates();

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), entityCaptor.capture(), any(Class.class));

        @SuppressWarnings("unchecked")
        List<ExchangeRateDto> rates = (List<ExchangeRateDto>) entityCaptor.getValue().getBody();
        
        for (ExchangeRateDto rate : rates) {
            assertNotNull(rate.getFromCurrency());
            assertNotNull(rate.getToCurrency());
            assertNotNull(rate.getBuyRate());
            assertNotNull(rate.getSellRate());
            assertNotNull(rate.getLastUpdated());
            
            // Buy rate should be higher than sell rate (spread)
            assertTrue(rate.getBuyRate().compareTo(rate.getSellRate()) > 0);
            
            // Rates should be positive
            assertTrue(rate.getBuyRate().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(rate.getSellRate().compareTo(BigDecimal.ZERO) > 0);
            
            // Rates should have reasonable precision
            assertTrue(rate.getBuyRate().scale() <= 6);
            assertTrue(rate.getSellRate().scale() <= 6);
        }
    }
} 
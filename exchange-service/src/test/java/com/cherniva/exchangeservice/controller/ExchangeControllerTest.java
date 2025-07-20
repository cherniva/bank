package com.cherniva.exchangeservice.controller;

import com.cherniva.common.dto.ExchangeRateDto;
import com.cherniva.common.dto.ExchangeRatesResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExchangeControllerTest {

    @InjectMocks
    private ExchangeController exchangeController;

    @BeforeEach
    void setUp() {
        // Reset the internal state before each test
        exchangeController = new ExchangeController();
    }

    @Test
    void getExchangeRates_DefaultRates_ReturnsValidResponse() {
        // Act
        ExchangeRatesResponseDto response = exchangeController.getExchangeRates();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Exchange rates retrieved successfully", response.getMessage());
        assertNotNull(response.getRates());
        assertEquals(3, response.getRates().size()); // RUB-USD, RUB-CNY, USD-CNY

        // Verify RUB-USD rate exists
        ExchangeRateDto rubUsd = response.getRates().stream()
                .filter(rate -> "RUB".equals(rate.getFromCurrency()) && "USD".equals(rate.getToCurrency()))
                .findFirst()
                .orElse(null);
        assertNotNull(rubUsd);
        assertTrue(rubUsd.getBuyRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(rubUsd.getSellRate().compareTo(BigDecimal.ZERO) > 0);

        // Verify RUB-CNY rate exists
        ExchangeRateDto rubCny = response.getRates().stream()
                .filter(rate -> "RUB".equals(rate.getFromCurrency()) && "CNY".equals(rate.getToCurrency()))
                .findFirst()
                .orElse(null);
        assertNotNull(rubCny);
        assertTrue(rubCny.getBuyRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(rubCny.getSellRate().compareTo(BigDecimal.ZERO) > 0);

        // Verify USD-CNY calculated rate exists
        ExchangeRateDto usdCny = response.getRates().stream()
                .filter(rate -> "USD".equals(rate.getFromCurrency()) && "CNY".equals(rate.getToCurrency()))
                .findFirst()
                .orElse(null);
        assertNotNull(usdCny);
        assertTrue(usdCny.getBuyRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(usdCny.getSellRate().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void updateExchangeRates_ValidRates_UpdatesSuccessfully() {
        // Arrange
        ExchangeRateDto newRate = new ExchangeRateDto();
        newRate.setFromCurrency("RUB");
        newRate.setToCurrency("USD");
        newRate.setBuyRate(BigDecimal.valueOf(0.0115));
        newRate.setSellRate(BigDecimal.valueOf(0.0110));
        newRate.setLastUpdated(LocalDateTime.now());

        List<ExchangeRateDto> newRates = List.of(newRate);

        // Act
        ExchangeRatesResponseDto response = exchangeController.updateExchangeRates(newRates);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Exchange rates updated successfully", response.getMessage());
        assertNotNull(response.getRates());
        assertTrue(response.getRates().size() > 0);

        // Verify the rate was updated
        ExchangeRatesResponseDto getResponse = exchangeController.getExchangeRates();
        ExchangeRateDto updatedRate = getResponse.getRates().stream()
                .filter(rate -> "RUB".equals(rate.getFromCurrency()) && "USD".equals(rate.getToCurrency()))
                .findFirst()
                .orElse(null);
        assertNotNull(updatedRate);
        assertEquals(BigDecimal.valueOf(0.0115), updatedRate.getBuyRate());
        assertEquals(BigDecimal.valueOf(0.0110), updatedRate.getSellRate());
    }

    @Test
    void updateExchangeRates_MultipleRates_UpdatesAllSuccessfully() {
        // Arrange
        ExchangeRateDto rubUsdRate = new ExchangeRateDto();
        rubUsdRate.setFromCurrency("RUB");
        rubUsdRate.setToCurrency("USD");
        rubUsdRate.setBuyRate(BigDecimal.valueOf(0.0115));
        rubUsdRate.setSellRate(BigDecimal.valueOf(0.0110));

        ExchangeRateDto rubCnyRate = new ExchangeRateDto();
        rubCnyRate.setFromCurrency("RUB");
        rubCnyRate.setToCurrency("CNY");
        rubCnyRate.setBuyRate(BigDecimal.valueOf(0.0820));
        rubCnyRate.setSellRate(BigDecimal.valueOf(0.0800));

        List<ExchangeRateDto> newRates = List.of(rubUsdRate, rubCnyRate);

        // Act
        ExchangeRatesResponseDto response = exchangeController.updateExchangeRates(newRates);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getRates().size() >= 2);
    }

    @Test
    void updateExchangeRates_EmptyList_HandlesGracefully() {
        // Arrange
        List<ExchangeRateDto> emptyRates = List.of();

        // Act
        ExchangeRatesResponseDto response = exchangeController.updateExchangeRates(emptyRates);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Exchange rates updated successfully", response.getMessage());
    }
} 
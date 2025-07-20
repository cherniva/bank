package com.cherniva.common.repo;

import com.cherniva.common.model.Currency;
import com.cherniva.common.model.ExchangeRate;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateRepoTest extends BaseRepositoryTest {

    @Test
    void findById_ExistingExchangeRate_ReturnsExchangeRate() {
        // Act
        Optional<ExchangeRate> result = exchangeRateRepo.findById(usdEurRate.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(usdEurRate.getId());
        assertThat(result.get().getSourceCurrency().getCode()).isEqualTo("USD");
        assertThat(result.get().getTargetCurrency().getCode()).isEqualTo("EUR");
        assertThat(result.get().getBuyPrice()).isEqualByComparingTo(new BigDecimal("0.85"));
        assertThat(result.get().getSellPrice()).isEqualByComparingTo(new BigDecimal("0.82"));
    }

    @Test
    void findById_NonExistentExchangeRate_ReturnsEmpty() {
        // Act
        Optional<ExchangeRate> result = exchangeRateRepo.findById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ReturnsAllExchangeRates() {
        // Arrange - Create additional exchange rate
        ExchangeRate eurUsdRate = new ExchangeRate();
        eurUsdRate.setSourceCurrency(eurCurrency);
        eurUsdRate.setTargetCurrency(usdCurrency);
        eurUsdRate.setBuyPrice(new BigDecimal("1.18"));
        eurUsdRate.setSellPrice(new BigDecimal("1.15"));
        exchangeRateRepo.save(eurUsdRate);
        entityManager.flush();

        // Act
        List<ExchangeRate> rates = exchangeRateRepo.findAll();

        // Assert
        assertThat(rates).hasSize(2);
        assertThat(rates).extracting(ExchangeRate::getId)
                .containsExactlyInAnyOrder(usdEurRate.getId(), eurUsdRate.getId());
    }

    @Test
    void save_NewExchangeRate_SavesSuccessfully() {
        // Arrange
        ExchangeRate newRate = new ExchangeRate();
        newRate.setSourceCurrency(usdCurrency);
        newRate.setTargetCurrency(rubCurrency);
        newRate.setBuyPrice(new BigDecimal("75.50"));
        newRate.setSellPrice(new BigDecimal("74.25"));

        // Act
        ExchangeRate savedRate = exchangeRateRepo.save(newRate);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(savedRate.getId()).isNotNull();
        
        Optional<ExchangeRate> retrieved = exchangeRateRepo.findById(savedRate.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getSourceCurrency().getCode()).isEqualTo("USD");
        assertThat(retrieved.get().getTargetCurrency().getCode()).isEqualTo("RUB");
        assertThat(retrieved.get().getBuyPrice()).isEqualByComparingTo(new BigDecimal("75.50"));
        assertThat(retrieved.get().getSellPrice()).isEqualByComparingTo(new BigDecimal("74.25"));
    }

    @Test
    void save_UpdateExistingExchangeRate_UpdatesSuccessfully() {
        // Arrange
        usdEurRate.setBuyPrice(new BigDecimal("0.88"));
        usdEurRate.setSellPrice(new BigDecimal("0.85"));

        // Act
        ExchangeRate updatedRate = exchangeRateRepo.save(usdEurRate);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<ExchangeRate> retrieved = exchangeRateRepo.findById(updatedRate.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getBuyPrice()).isEqualByComparingTo(new BigDecimal("0.88"));
        assertThat(retrieved.get().getSellPrice()).isEqualByComparingTo(new BigDecimal("0.85"));
    }

    @Test
    void delete_ExistingExchangeRate_DeletesSuccessfully() {
        // Arrange
        Long rateId = usdEurRate.getId();

        // Act
        exchangeRateRepo.delete(usdEurRate);
        entityManager.flush();

        // Assert
        Optional<ExchangeRate> result = exchangeRateRepo.findById(rateId);
        assertThat(result).isEmpty();
    }

    @Test
    void save_ExchangeRateWithSameCurrencies_SavesSuccessfully() {
        // Arrange - Same currency exchange rate (unusual but valid)
        ExchangeRate sameRate = new ExchangeRate();
        sameRate.setSourceCurrency(usdCurrency);
        sameRate.setTargetCurrency(usdCurrency);
        sameRate.setBuyPrice(new BigDecimal("1.0"));
        sameRate.setSellPrice(new BigDecimal("1.0"));

        // Act
        ExchangeRate savedRate = exchangeRateRepo.save(sameRate);
        entityManager.flush();

        // Assert
        assertThat(savedRate.getId()).isNotNull();
        assertThat(savedRate.getSourceCurrency().getCode()).isEqualTo("USD");
        assertThat(savedRate.getTargetCurrency().getCode()).isEqualTo("USD");
    }

    @Test
    void save_ExchangeRateWithZeroPrices_SavesSuccessfully() {
        // Arrange
        ExchangeRate zeroRate = new ExchangeRate();
        zeroRate.setSourceCurrency(eurCurrency);
        zeroRate.setTargetCurrency(rubCurrency);
        zeroRate.setBuyPrice(BigDecimal.ZERO);
        zeroRate.setSellPrice(BigDecimal.ZERO);

        // Act
        ExchangeRate savedRate = exchangeRateRepo.save(zeroRate);
        entityManager.flush();

        // Assert
        assertThat(savedRate.getId()).isNotNull();
        assertThat(savedRate.getBuyPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedRate.getSellPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void save_ExchangeRateWithNegativePrices_SavesSuccessfully() {
        // Arrange - Negative rates (unusual but possible in some financial scenarios)
        ExchangeRate negativeRate = new ExchangeRate();
        negativeRate.setSourceCurrency(eurCurrency);
        negativeRate.setTargetCurrency(rubCurrency);
        negativeRate.setBuyPrice(new BigDecimal("-0.10"));
        negativeRate.setSellPrice(new BigDecimal("-0.05"));

        // Act
        ExchangeRate savedRate = exchangeRateRepo.save(negativeRate);
        entityManager.flush();

        // Assert
        assertThat(savedRate.getId()).isNotNull();
        assertThat(savedRate.getBuyPrice()).isEqualByComparingTo(new BigDecimal("-0.10"));
        assertThat(savedRate.getSellPrice()).isEqualByComparingTo(new BigDecimal("-0.05"));
    }

    @Test
    void save_ExchangeRateWithHighPrecisionPrices_SavesSuccessfully() {
        // Arrange
        ExchangeRate preciseRate = new ExchangeRate();
        preciseRate.setSourceCurrency(usdCurrency);
        preciseRate.setTargetCurrency(eurCurrency);
        preciseRate.setBuyPrice(new BigDecimal("0.123456789"));
        preciseRate.setSellPrice(new BigDecimal("0.987654321"));

        // Act
        ExchangeRate savedRate = exchangeRateRepo.save(preciseRate);
        entityManager.flush();

        // Assert
        assertThat(savedRate.getId()).isNotNull();
        assertThat(savedRate.getBuyPrice()).isEqualByComparingTo(new BigDecimal("0.123456789"));
        assertThat(savedRate.getSellPrice()).isEqualByComparingTo(new BigDecimal("0.987654321"));
    }

    @Test
    void save_ExchangeRateWithLargeValues_SavesSuccessfully() {
        // Arrange
        ExchangeRate largeRate = new ExchangeRate();
        largeRate.setSourceCurrency(rubCurrency);
        largeRate.setTargetCurrency(usdCurrency);
        largeRate.setBuyPrice(new BigDecimal("999999.99"));
        largeRate.setSellPrice(new BigDecimal("888888.88"));

        // Act
        ExchangeRate savedRate = exchangeRateRepo.save(largeRate);
        entityManager.flush();

        // Assert
        assertThat(savedRate.getId()).isNotNull();
        assertThat(savedRate.getBuyPrice()).isEqualByComparingTo(new BigDecimal("999999.99"));
        assertThat(savedRate.getSellPrice()).isEqualByComparingTo(new BigDecimal("888888.88"));
    }

    @Test
    void findAll_VerifyRelationshipsLoaded() {
        // Act
        List<ExchangeRate> rates = exchangeRateRepo.findAll();

        // Assert
        for (ExchangeRate rate : rates) {
            assertThat(rate.getSourceCurrency()).isNotNull();
            assertThat(rate.getSourceCurrency().getCode()).isNotBlank();
            assertThat(rate.getTargetCurrency()).isNotNull();
            assertThat(rate.getTargetCurrency().getCode()).isNotBlank();
        }
    }

    @Test
    void save_MultipleExchangeRatesForSameCurrencyPair_SavesSuccessfully() {
        // Arrange - Multiple rates for same currency pair (e.g., different time periods)
        ExchangeRate rate1 = new ExchangeRate();
        rate1.setSourceCurrency(eurCurrency);
        rate1.setTargetCurrency(rubCurrency);
        rate1.setBuyPrice(new BigDecimal("85.0"));
        rate1.setSellPrice(new BigDecimal("84.0"));

        ExchangeRate rate2 = new ExchangeRate();
        rate2.setSourceCurrency(eurCurrency);
        rate2.setTargetCurrency(rubCurrency);
        rate2.setBuyPrice(new BigDecimal("86.0"));
        rate2.setSellPrice(new BigDecimal("85.0"));

        // Act
        ExchangeRate savedRate1 = exchangeRateRepo.save(rate1);
        ExchangeRate savedRate2 = exchangeRateRepo.save(rate2);
        entityManager.flush();

        // Assert
        assertThat(savedRate1.getId()).isNotNull();
        assertThat(savedRate2.getId()).isNotNull();
        assertThat(savedRate1.getId()).isNotEqualTo(savedRate2.getId());
        
        List<ExchangeRate> allRates = exchangeRateRepo.findAll();
        assertThat(allRates).hasSize(3); // Original + 2 new ones
    }
} 
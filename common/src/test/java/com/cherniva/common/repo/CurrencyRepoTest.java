package com.cherniva.common.repo;

import com.cherniva.common.model.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyRepoTest extends BaseRepositoryTest {

    @Test
    void findById_ExistingCurrency_ReturnsCurrency() {
        // Act
        Optional<Currency> result = currencyRepo.findById(usdCurrency.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(usdCurrency.getId());
        assertThat(result.get().getCode()).isEqualTo("USD");
        assertThat(result.get().getName()).isEqualTo("US Dollar");
    }

    @Test
    void findById_NonExistentCurrency_ReturnsEmpty() {
        // Act
        Optional<Currency> result = currencyRepo.findById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findCurrencyByCode_ExistingCode_ReturnsCurrency() {
        // Act
        Optional<Currency> result = currencyRepo.findCurrencyByCode("USD");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("USD");
        assertThat(result.get().getName()).isEqualTo("US Dollar");
        assertThat(result.get().getId()).isEqualTo(usdCurrency.getId());
    }

    @Test
    void findCurrencyByCode_NonExistentCode_ReturnsEmpty() {
        // Act
        Optional<Currency> result = currencyRepo.findCurrencyByCode("GBP");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findCurrencyByCode_NullCode_ReturnsEmpty() {
        // Act
        Optional<Currency> result = currencyRepo.findCurrencyByCode(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findCurrencyByCode_EmptyCode_ReturnsEmpty() {
        // Act
        Optional<Currency> result = currencyRepo.findCurrencyByCode("");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findCurrencyByCode_CaseSensitive_ReturnsEmpty() {
        // Act
        Optional<Currency> result = currencyRepo.findCurrencyByCode("usd");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ReturnsAllCurrencies() {
        // Act
        List<Currency> currencies = currencyRepo.findAll();

        // Assert
        assertThat(currencies).hasSize(3);
        assertThat(currencies).extracting(Currency::getCode)
                .containsExactlyInAnyOrder("USD", "EUR", "RUB");
    }

    @Test
    void save_NewCurrency_SavesSuccessfully() {
        // Arrange
        Currency newCurrency = new Currency();
        newCurrency.setId(11L);
        newCurrency.setCode("GBP");
        newCurrency.setName("British Pound");

        // Act
        Currency savedCurrency = currencyRepo.save(newCurrency);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(savedCurrency.getId()).isNotNull();
        
        Optional<Currency> retrieved = currencyRepo.findCurrencyByCode("GBP");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("British Pound");
    }

    @Test
    void save_UpdateExistingCurrency_UpdatesSuccessfully() {
        // Arrange
        usdCurrency.setName("United States Dollar");

        // Act
        Currency updatedCurrency = currencyRepo.save(usdCurrency);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Currency> retrieved = currencyRepo.findCurrencyByCode("USD");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("United States Dollar");
    }

    @Test
    void delete_ExistingCurrency_DeletesSuccessfully() {
        // Arrange
        Currency jpy = new Currency();
        jpy.setId(5L);
        jpy.setCode("JPY");
        jpy.setName("Japanese Yen");
        jpy = currencyRepo.save(jpy);
        entityManager.flush();
        
        String currencyCode = jpy.getCode();

        // Act
        currencyRepo.delete(jpy);
        entityManager.flush();

        // Assert
        Optional<Currency> result = currencyRepo.findCurrencyByCode(currencyCode);
        assertThat(result).isEmpty();
    }

    @Test
    void save_CurrencyWithLongCode_SavesSuccessfully() {
        // Arrange - Test edge case with longer currency code
        Currency longCodeCurrency = new Currency();
        longCodeCurrency.setId(8L);
        longCodeCurrency.setCode("CUSTOM");
        longCodeCurrency.setName("Custom Currency");

        // Act
        Currency savedCurrency = currencyRepo.save(longCodeCurrency);
        entityManager.flush();

        // Assert
        assertThat(savedCurrency.getId()).isNotNull();
        assertThat(savedCurrency.getCode()).isEqualTo("CUSTOM");
    }

    @Test
    void findCurrencyByCode_WithSpecialCharacters_FindsCurrency() {
        // Arrange
        Currency specialCurrency = new Currency();
        specialCurrency.setId(10L);
        specialCurrency.setCode("X-USD");
        specialCurrency.setName("Special USD");
        
        currencyRepo.save(specialCurrency);
        entityManager.flush();

        // Act
        Optional<Currency> result = currencyRepo.findCurrencyByCode("X-USD");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("X-USD");
    }
} 
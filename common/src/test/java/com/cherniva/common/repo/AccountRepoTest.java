package com.cherniva.common.repo;

import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.model.UserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountRepoTest extends BaseRepositoryTest {

    @Test
    void findById_ExistingAccount_ReturnsAccount() {
        // Act
        Optional<Account> result = accountRepo.findById(usdAccount.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(usdAccount.getId());
        assertThat(result.get().getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.get().getCurrency().getCode()).isEqualTo("USD");
        assertThat(result.get().getUserDetails().getUsername()).isEqualTo("testuser1");
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void findById_NonExistentAccount_ReturnsEmpty() {
        // Act
        Optional<Account> result = accountRepo.findById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ReturnsAllAccounts() {
        // Act
        List<Account> accounts = accountRepo.findAll();

        // Assert
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(Account::getId)
                .containsExactlyInAnyOrder(usdAccount.getId(), eurAccount.getId());
    }

    @Test
    void save_NewAccount_SavesSuccessfully() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setUserDetails(testUser2);
        newAccount.setCurrency(rubCurrency);
        newAccount.setAmount(new BigDecimal("2000.00"));
        newAccount.setActive(true);

        // Act
        Account savedAccount = accountRepo.save(newAccount);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(savedAccount.getId()).isNotNull();
        
        Optional<Account> retrieved = accountRepo.findById(savedAccount.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getUserDetails().getUsername()).isEqualTo("testuser2");
        assertThat(retrieved.get().getCurrency().getCode()).isEqualTo("RUB");
        assertThat(retrieved.get().getAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(retrieved.get().isActive()).isTrue();
    }

    @Test
    void save_UpdateExistingAccount_UpdatesSuccessfully() {
        // Arrange
        usdAccount.setAmount(new BigDecimal("1500.00"));
        usdAccount.setActive(false);

        // Act
        Account updatedAccount = accountRepo.save(usdAccount);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Account> retrieved = accountRepo.findById(updatedAccount.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(retrieved.get().isActive()).isFalse();
    }

    @Test
    void delete_ExistingAccount_DeletesSuccessfully() {
        // Arrange
        Long accountId = usdAccount.getId();

        // Act
        accountRepo.delete(usdAccount);
        entityManager.flush();

        // Assert
        Optional<Account> result = accountRepo.findById(accountId);
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_VerifyRelationshipsLoaded() {
        // Act
        List<Account> accounts = accountRepo.findAll();

        // Assert
        for (Account account : accounts) {
            assertThat(account.getUserDetails()).isNotNull();
            assertThat(account.getUserDetails().getUsername()).isNotBlank();
            assertThat(account.getCurrency()).isNotNull();
            assertThat(account.getCurrency().getCode()).isNotBlank();
        }
    }

    @Test
    void save_AccountWithNegativeAmount_SavesSuccessfully() {
        // Arrange - Bank accounts can have negative amounts (overdraft)
        Account overdraftAccount = new Account();
        overdraftAccount.setUserDetails(testUser2);
        overdraftAccount.setCurrency(usdCurrency);
        overdraftAccount.setAmount(new BigDecimal("-100.00"));
        overdraftAccount.setActive(true);

        // Act
        Account savedAccount = accountRepo.save(overdraftAccount);
        entityManager.flush();

        // Assert
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
    }

    @Test
    void save_AccountWithLargeAmount_SavesSuccessfully() {
        // Arrange
        Account largeAccount = new Account();
        largeAccount.setUserDetails(testUser2);
        largeAccount.setCurrency(eurCurrency);
        largeAccount.setAmount(new BigDecimal("999999999.99"));
        largeAccount.setActive(true);

        // Act
        Account savedAccount = accountRepo.save(largeAccount);
        entityManager.flush();

        // Assert
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAmount()).isEqualByComparingTo(new BigDecimal("999999999.99"));
    }
} 
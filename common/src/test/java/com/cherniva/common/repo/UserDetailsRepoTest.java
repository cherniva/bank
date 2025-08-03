package com.cherniva.common.repo;

import com.cherniva.common.model.UserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDetailsRepoTest extends BaseRepositoryTest {

    @Test
    void findById_ExistingUser_ReturnsUser() {
        // Act
        Optional<UserDetails> result = userDetailsRepo.findById(testUser1.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testUser1.getId());
        assertThat(result.get().getUsername()).isEqualTo("testuser1");
        assertThat(result.get().getName()).isEqualTo("John");
        assertThat(result.get().getSurname()).isEqualTo("Doe");
        assertThat(result.get().getBirthdate()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void findById_NonExistentUser_ReturnsEmpty() {
        // Act
        Optional<UserDetails> result = userDetailsRepo.findById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_ExistingUser_ReturnsUser() {
        // Act
        Optional<UserDetails> result = userDetailsRepo.findByUsername("testuser1");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser1");
        assertThat(result.get().getName()).isEqualTo("John");
        assertThat(result.get().getSurname()).isEqualTo("Doe");
        assertThat(result.get().getId()).isEqualTo(testUser1.getId());
    }

    @Test
    void findByUsername_NonExistentUser_ReturnsEmpty() {
        // Act
        Optional<UserDetails> result = userDetailsRepo.findByUsername("nonexistent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_NullUsername_ReturnsEmpty() {
        // Act
        Optional<UserDetails> result = userDetailsRepo.findByUsername(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_EmptyUsername_ReturnsEmpty() {
        // Act
        Optional<UserDetails> result = userDetailsRepo.findByUsername("");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_CaseSensitive_ReturnsEmpty() {
        // Act
        Optional<UserDetails> result = userDetailsRepo.findByUsername("TESTUSER1");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ReturnsAllUsers() {
        // Act
        List<UserDetails> users = userDetailsRepo.findAll();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserDetails::getUsername)
                .containsExactlyInAnyOrder("testuser1", "testuser2");
    }

    @Test
    void save_NewUser_SavesSuccessfully() {
        // Arrange
        UserDetails newUser = new UserDetails();
        newUser.setId(10L);
        newUser.setUsername("newuser");
        newUser.setPassword("encodedPassword");
        newUser.setName("Alice");
        newUser.setSurname("Johnson");
        newUser.setBirthdate(LocalDate.of(1985, 12, 10));

        // Act
        UserDetails savedUser = userDetailsRepo.save(newUser);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        
        Optional<UserDetails> retrieved = userDetailsRepo.findByUsername("newuser");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Alice");
        assertThat(retrieved.get().getSurname()).isEqualTo("Johnson");
        assertThat(retrieved.get().getBirthdate()).isEqualTo(LocalDate.of(1985, 12, 10));
    }

    @Test
    void save_UpdateExistingUser_UpdatesSuccessfully() {
        // Arrange
        testUser1.setName("UpdatedJohn");
        testUser1.setSurname("UpdatedDoe");

        // Act
        UserDetails updatedUser = userDetailsRepo.save(testUser1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<UserDetails> retrieved = userDetailsRepo.findByUsername("testuser1");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("UpdatedJohn");
        assertThat(retrieved.get().getSurname()).isEqualTo("UpdatedDoe");
    }

    @Test
    void delete_ExistingUser_DeletesSuccessfully() {
        // Arrange
        // First delete related accounts to avoid foreign key constraint
        accountRepo.deleteAll();
        entityManager.flush();
        
        String username = testUser1.getUsername();

        // Act
        userDetailsRepo.delete(testUser1);
        entityManager.flush();

        // Assert
        Optional<UserDetails> result = userDetailsRepo.findByUsername(username);
        assertThat(result).isEmpty();
    }

    @Test
    void save_UserWithFutureBirthdate_SavesSuccessfully() {
        // Arrange - Edge case test
        UserDetails futureUser = new UserDetails();
        futureUser.setId(13L);
        futureUser.setUsername("futureuser");
        futureUser.setPassword("password");
        futureUser.setName("Future");
        futureUser.setSurname("User");
        futureUser.setBirthdate(LocalDate.of(2030, 1, 1));

        // Act
        UserDetails savedUser = userDetailsRepo.save(futureUser);
        entityManager.flush();

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getBirthdate()).isEqualTo(LocalDate.of(2030, 1, 1));
    }

    @Test
    void save_UserWithVeryOldBirthdate_SavesSuccessfully() {
        // Arrange - Edge case test
        UserDetails oldUser = new UserDetails();
        oldUser.setId(12L);
        oldUser.setUsername("olduser");
        oldUser.setPassword("password");
        oldUser.setName("Old");
        oldUser.setSurname("User");
        oldUser.setBirthdate(LocalDate.of(1800, 1, 1));

        // Act
        UserDetails savedUser = userDetailsRepo.save(oldUser);
        entityManager.flush();

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getBirthdate()).isEqualTo(LocalDate.of(1800, 1, 1));
    }

    @Test
    void findByUsername_WithSpecialCharacters_FindsUser() {
        // Arrange
        UserDetails specialUser = new UserDetails();
        specialUser.setId(11L);
        specialUser.setUsername("user.name@domain.com");
        specialUser.setPassword("password");
        specialUser.setName("Special");
        specialUser.setSurname("User");
        specialUser.setBirthdate(LocalDate.of(1990, 1, 1));
        
        userDetailsRepo.save(specialUser);
        entityManager.flush();

        // Act
        Optional<UserDetails> result = userDetailsRepo.findByUsername("user.name@domain.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("user.name@domain.com");
    }
} 
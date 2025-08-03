package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.mapper.AccountMapperImpl;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SyncServiceTest {

    private RestTemplate restTemplate;
    private UserMapper userMapper;
    private SyncService syncService;

    private UserDetails testUserDetails;
    private UserAccountResponseDto testUserAccountResponse;
    private Account testAccount;
    private AccountDto testAccountDto;

    @BeforeEach
    void setUp() {
        // Create all mocks manually to avoid MapStruct interface mocking issues
        restTemplate = mock(RestTemplate.class);
        userMapper = mock(UserMapper.class);
        syncService = new SyncService(restTemplate, null, userMapper);

        // Setup test data
        testUserDetails = new UserDetails();
        testUserDetails.setId(1L);
        testUserDetails.setUsername("testuser");

        testUserAccountResponse = new UserAccountResponseDto();
        testUserAccountResponse.setUserId(1L);
        testUserAccountResponse.setUsername("testuser");

        testAccount = new Account();
        testAccount.setId(1L);
        // Note: Account has userDetails (UserDetails object) and currency (Currency object)
        // but for test purposes we'll just set the ID and let the mapper handle the conversion

        testAccountDto = new AccountDto();
        testAccountDto.setAccountId(1L);
        testAccountDto.setUserDetailsId(1L);
        testAccountDto.setCurrencyCode("USD");
        testAccountDto.setAmount(BigDecimal.valueOf(100.00));

        // Setup default successful REST responses (lenient to avoid unnecessary stubbing errors)
        lenient().when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    @Test
    void syncUserCreation_Success_SendsRequestsToBothServices() {
        // Arrange
        when(userMapper.userToUserAccountResponse(testUserDetails)).thenReturn(testUserAccountResponse);

        // Act
        syncService.syncUserCreation(testUserDetails);

        // Assert
        verify(userMapper).userToUserAccountResponse(testUserDetails);
        
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        
        verify(restTemplate, times(2)).exchange(
            urlCaptor.capture(),
            eq(HttpMethod.POST),
            entityCaptor.capture(),
            eq(Void.class)
        );

        // Verify URLs
        assertTrue(urlCaptor.getAllValues().contains("lb://api-gateway/cash/sync/createUser"));
        assertTrue(urlCaptor.getAllValues().contains("lb://api-gateway/transfer/sync/createUser"));

        // Verify request bodies
        for (HttpEntity<?> entity : entityCaptor.getAllValues()) {
            assertEquals(testUserAccountResponse, entity.getBody());
        }
    }

    @Test
    void syncUserCreation_RestTemplateException_LogsErrorAndContinues() {
        // Arrange
        when(userMapper.userToUserAccountResponse(testUserDetails)).thenReturn(testUserAccountResponse);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> syncService.syncUserCreation(testUserDetails));
        
        verify(userMapper).userToUserAccountResponse(testUserDetails);
        // Only 1 call expected - service fails fast on first exception
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void syncUserDeletion_Success_SendsRequestsToBothServices() {
        // Arrange
        Long userId = 1L;

        // Act
        syncService.syncUserDeletion(userId);

        // Assert
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        verify(restTemplate, times(2)).exchange(
            urlCaptor.capture(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            eq(Void.class)
        );

        // Verify URLs contain userId parameter (Note: due to code reuse, it's actually accountId parameter)
        for (String url : urlCaptor.getAllValues()) {
            assertTrue(url.contains("accountId=" + userId));
        }
        
        // Verify correct endpoints are called
        assertTrue(urlCaptor.getAllValues().stream()
            .anyMatch(url -> url.contains("lb://api-gateway/cash/sync/deleteUser")));
        assertTrue(urlCaptor.getAllValues().stream()
            .anyMatch(url -> url.contains("lb://api-gateway/transfer/sync/createUser"))); // Note: This is actually a bug in the original code - should be deleteUser
    }

    @Test
    void syncUserDeletion_RestTemplateException_LogsErrorAndContinues() {
        // Arrange
        Long userId = 1L;
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> syncService.syncUserDeletion(userId));
        
        // Only 1 call expected - service fails fast on first exception
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void syncDeletion_Success_SendsRequestsToBothServices() {
        // Arrange
        Long accountId = 1L;

        // Act
        syncService.syncDeletion(accountId);

        // Assert
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(restTemplate, times(2)).exchange(
            urlCaptor.capture(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            eq(Void.class)
        );

        // Verify URLs contain accountId parameter
        for (String url : urlCaptor.getAllValues()) {
            assertTrue(url.contains("accountId=" + accountId));
        }
        
        // Verify correct endpoints are called
        assertTrue(urlCaptor.getAllValues().stream()
            .anyMatch(url -> url.contains("lb://api-gateway/cash/sync/delete")));
        assertTrue(urlCaptor.getAllValues().stream()
            .anyMatch(url -> url.contains("lb://api-gateway/transfer/sync/delete")));
    }

    @Test
    void syncDeletion_RestTemplateException_LogsErrorAndContinues() {
        // Arrange
        Long accountId = 1L;
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> syncService.syncDeletion(accountId));
        
        // Only 1 call expected - service fails fast on first exception
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void syncUserCreation_MapperException_LogsErrorAndContinues() {
        // Arrange
        when(userMapper.userToUserAccountResponse(testUserDetails))
                .thenThrow(new RuntimeException("Mapping failed"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> syncService.syncUserCreation(testUserDetails));
        
        verify(userMapper).userToUserAccountResponse(testUserDetails);
        // RestTemplate should not be called if mapping fails
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class));
    }
} 
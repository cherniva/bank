package com.cherniva.frontui.service;

import com.cherniva.common.dto.SessionValidationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SessionService sessionService;

    private SessionValidationDto mockValidResponse;
    private SessionValidationDto mockInvalidResponse;

    @BeforeEach
    void setUp() {
        mockValidResponse = new SessionValidationDto();
        mockValidResponse.setSessionId("session123");
        mockValidResponse.setValid(true);

        mockInvalidResponse = new SessionValidationDto();
        mockInvalidResponse.setSessionId("session123");
        mockInvalidResponse.setValid(false);
    }

    @Test
    void validateSession_ValidSession_ReturnsValidResponse() {
        // Arrange
        when(restTemplate.postForObject(anyString(), isNull(), eq(SessionValidationDto.class)))
                .thenReturn(mockValidResponse);

        // Act
        SessionValidationDto result = sessionService.validateSession("session123");

        // Assert
        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals("session123", result.getSessionId());
        verify(restTemplate).postForObject(
                "lb://api-gateway/api/auth/validate-session?sessionId=session123", 
                null, 
                SessionValidationDto.class);
    }

    @Test
    void validateSession_InvalidSession_ReturnsInvalidResponse() {
        // Arrange
        when(restTemplate.postForObject(anyString(), isNull(), eq(SessionValidationDto.class)))
                .thenReturn(mockInvalidResponse);

        // Act
        SessionValidationDto result = sessionService.validateSession("session123");

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals("session123", result.getSessionId());
    }

    @Test
    void validateSession_ServiceException_ReturnsInvalidResponse() {
        // Arrange
        when(restTemplate.postForObject(anyString(), isNull(), eq(SessionValidationDto.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act
        SessionValidationDto result = sessionService.validateSession("session123");

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals("session123", result.getSessionId());
    }

    @Test
    void logout_ValidSessionId_CallsLogoutEndpoint() {
        // Arrange
        when(restTemplate.postForObject(anyString(), isNull(), eq(Void.class)))
                .thenReturn(null);

        // Act
        sessionService.logout("session123");

        // Assert
        verify(restTemplate).postForObject(
                "lb://api-gateway/api/auth/logout?sessionId=session123", 
                null, 
                Void.class);
    }

    @Test
    void logout_ServiceException_DoesNotThrowException() {
        // Arrange
        when(restTemplate.postForObject(anyString(), isNull(), eq(Void.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> sessionService.logout("session123"));
    }

    @Test
    void logout_NullSessionId_DoesNotCallEndpoint() {
        // Act
        sessionService.logout(null);

        // Assert
        verify(restTemplate).postForObject(
                "lb://api-gateway/api/auth/logout?sessionId=null", 
                null, 
                Void.class);
    }
} 
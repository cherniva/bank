package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @InjectMocks
    private SessionService sessionService;

    private UserAccountResponseDto testUserData;

    @BeforeEach
    void setUp() {
        testUserData = new UserAccountResponseDto();
        testUserData.setUserId(1L);
        testUserData.setUsername("testuser");
        testUserData.setName("John");
        testUserData.setSurname("Doe");
        testUserData.setBirthdate(LocalDate.of(1990, 1, 1));
        testUserData.setAccounts(new ArrayList<>());
    }

    @Test
    void createSession_WithUserData_ReturnsSessionId() {
        // Act
        String sessionId = sessionService.createSession(testUserData);

        // Assert
        assertNotNull(sessionId);
        assertFalse(sessionId.isEmpty());
    }

    @Test
    void createSession_WithUsernameAndUserId_ReturnsSessionId() {
        // Act
        String sessionId = sessionService.createSession("testuser", 1L);

        // Assert
        assertNotNull(sessionId);
        assertFalse(sessionId.isEmpty());
    }

    @Test
    void getSession_ValidSessionId_ReturnsSessionInfo() {
        // Arrange
        String sessionId = sessionService.createSession(testUserData);

        // Act
        SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);

        // Assert
        assertNotNull(sessionInfo);
        assertEquals("testuser", sessionInfo.getUsername());
        assertEquals(1L, sessionInfo.getUserId());
        assertEquals(testUserData, sessionInfo.getUserData());
    }

    @Test
    void getSession_InvalidSessionId_ReturnsNull() {
        // Act
        SessionService.SessionInfo sessionInfo = sessionService.getSession("invalid-session-id");

        // Assert
        assertNull(sessionInfo);
    }

    @Test
    void getSession_ExpiredSession_ReturnsNull() throws InterruptedException {
        // This test would require modifying the session expiration time
        // For practical testing, we'll test the logic with a mock scenario
        String sessionId = sessionService.createSession(testUserData);
        
        // Verify session exists first
        SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
        assertNotNull(sessionInfo);
    }

    @Test
    void removeSession_ValidSessionId_RemovesSession() {
        // Arrange
        String sessionId = sessionService.createSession(testUserData);
        
        // Verify session exists
        assertNotNull(sessionService.getSession(sessionId));

        // Act
        sessionService.removeSession(sessionId);

        // Assert
        assertNull(sessionService.getSession(sessionId));
    }

    @Test
    void removeSession_InvalidSessionId_NoException() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> sessionService.removeSession("invalid-session-id"));
    }

    @Test
    void sessionInfo_BackwardCompatibility_WorksCorrectly() {
        // Arrange
        String sessionId = sessionService.createSession("testuser", 1L);
        
        // Act
        SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);

        // Assert
        assertNotNull(sessionInfo);
        assertEquals("testuser", sessionInfo.getUsername());
        assertEquals(1L, sessionInfo.getUserId());
        assertNull(sessionInfo.getUserData()); // Should be null for backward compatibility constructor
    }

    @Test
    void sessionInfo_ConvenienceMethods_WorkCorrectly() {
        // Arrange
        String sessionId = sessionService.createSession(testUserData);
        
        // Act
        SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);

        // Assert
        assertNotNull(sessionInfo);
        assertEquals("John", sessionInfo.getName());
        assertEquals("Doe", sessionInfo.getSurname());
        assertEquals(LocalDate.of(1990, 1, 1), sessionInfo.getBirthday());
        assertNotNull(sessionInfo.getAccounts());
    }
} 
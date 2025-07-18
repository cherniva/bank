package com.cherniva.accountsservice.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SessionService {
    
    // In-memory session storage (in production, use Redis or database)
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    
    public String createSession(String username, Long userId) {
        String sessionId = UUID.randomUUID().toString();
        SessionInfo sessionInfo = new SessionInfo(username, userId, System.currentTimeMillis());
        sessions.put(sessionId, sessionInfo);
        return sessionId;
    }
    
    public String createSession(UserAccountResponseDto userData) {
        String sessionId = UUID.randomUUID().toString();
        SessionInfo sessionInfo = new SessionInfo(userData, System.currentTimeMillis());
        sessions.put(sessionId, sessionInfo);
        return sessionId;
    }
    
    public SessionInfo getSession(String sessionId) {
        SessionInfo session = sessions.get(sessionId);
        if (session != null && !isSessionExpired(session)) {
            return session;
        }
        if (session != null) {
            sessions.remove(sessionId); // Remove expired session
        }
        return null;
    }
    
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
    
    private boolean isSessionExpired(SessionInfo session) {
        // Session expires after 30 minutes
        long currentTime = System.currentTimeMillis();
        return (currentTime - session.getCreatedAt()) > 30 * 60 * 1000;
    }
    
    public static class SessionInfo {
        private final String username;
        private final Long userId;
        private final UserAccountResponseDto userData;
        private final long createdAt;
        
        // Constructor for backward compatibility
        public SessionInfo(String username, Long userId, long createdAt) {
            this.username = username;
            this.userId = userId;
            this.userData = null;
            this.createdAt = createdAt;
        }
        
        // Constructor with complete user data
        public SessionInfo(UserAccountResponseDto userData, long createdAt) {
            this.username = userData.getUsername();
            this.userId = userData.getUserId();
            this.userData = userData;
            this.createdAt = createdAt;
        }
        
        public String getUsername() {
            return username;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public UserAccountResponseDto getUserData() {
            return userData;
        }
        
        public long getCreatedAt() {
            return createdAt;
        }
        
        // Convenience methods for backward compatibility
        public String getName() {
            return userData != null ? userData.getName() : null;
        }
        
        public String getSurname() {
            return userData != null ? userData.getSurname() : null;
        }
        
        public java.time.LocalDate getBirthday() {
            return userData != null ? userData.getBirthday() : null;
        }
        
        public java.util.List<com.cherniva.common.dto.AccountDto> getAccounts() {
            return userData != null ? userData.getAccounts() : null;
        }
    }
} 
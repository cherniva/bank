package com.cherniva.accountsservice.service;

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
        private final long createdAt;
        
        public SessionInfo(String username, Long userId, long createdAt) {
            this.username = username;
            this.userId = userId;
            this.createdAt = createdAt;
        }
        
        public String getUsername() {
            return username;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public long getCreatedAt() {
            return createdAt;
        }
    }
} 
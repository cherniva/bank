package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return valid session data when session exists"
    
    request {
        method POST()
        url "/api/auth/validate-session"
        urlPath("/api/auth/validate-session") {
            queryParameters {
                parameter "sessionId": "test-session-123"
            }
        }
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
            sessionId: "test-session-123",
            valid: true,
            userData: [
                userId: 1,
                username: "testuser",
                name: "John",
                surname: "Doe"
            ]
        )
    }
} 
package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user data when login is successful"
    
    request {
        method POST()
        url "/api/auth/login"
        headers {
            contentType(applicationJson())
        }
        body(
            username: "testuser",
            password: "password123"
        )
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
            userId: 1,
            username: "testuser",
            name: "John",
            surname: "Doe",
            sessionId: "test-session-123"
        )
    }
} 
package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return invalid session when session does not exist"
    
    request {
        method POST()
        url "/api/auth/validate-session"
        urlPath("/api/auth/validate-session") {
            queryParameters {
                parameter "sessionId": "invalid-session"
            }
        }
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
            sessionId: "invalid-session",
            valid: false,
            userData: null
        )
    }
} 
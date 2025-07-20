package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return bad request when login credentials are invalid"
    
    request {
        method POST()
        url "/api/auth/login"
        headers {
            contentType(applicationJson())
        }
        body(
            username: "wronguser",
            password: "wrongpassword"
        )
    }
    
    response {
        status BAD_REQUEST()
    }
} 
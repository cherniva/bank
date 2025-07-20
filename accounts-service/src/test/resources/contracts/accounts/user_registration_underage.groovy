package contracts.accounts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should reject registration when user is under 18 years old"
    
    request {
        method POST()
        url "/api/accounts/register"
        headers {
            contentType(applicationJson())
        }
        body(
            username: "younguser",
            password: "password123",
            name: "Young",
            surname: "User",
            birthdate: "2010-01-01"
        )
    }
    
    response {
        status BAD_REQUEST()
    }
} 
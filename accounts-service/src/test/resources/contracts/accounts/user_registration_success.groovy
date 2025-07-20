package contracts.accounts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should register user successfully when valid data is provided"
    
    request {
        method POST()
        url "/api/accounts/register"
        headers {
            contentType(applicationJson())
        }
        body(
            username: "newuser",
            password: "password123",
            name: "Jane",
            surname: "Smith",
            birthdate: "1995-05-15"
        )
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
            userId: anyPositiveInt(),
            username: "newuser",
            name: "Jane",
            surname: "Smith",
            accounts: []
        )
    }
} 
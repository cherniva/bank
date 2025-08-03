package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should register a new user"
    request {
        method POST()
        url "/api/users/register"
        headers {
            contentType(applicationJson())
        }
        body([
            username: "testuser",
            password: "password123",
            name: "John",
            surname: "Doe",
            birthdate: "1990-01-01"
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            userId: anyPositiveInt(),
            username: "testuser",
            name: "John",
            surname: "Doe",
            birthdate: "1990-01-01",
            accounts: []
        ])
    }
} 
package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should create a new user with accounts"
    request {
        method POST()
        url "/cash/sync/createUser"
        headers {
            contentType(applicationJson())
        }
        body([
            userId: 1,
            username: "testuser",
            name: "Test",
            surname: "User", 
            birthdate: "1990-01-01",
            accounts: [
                [
                    accountId: 1001,
                    currencyCode: "USD",
                    amount: 100.00,
                    active: true
                ],
                [
                    accountId: 1002,
                    currencyCode: "CNY",
                    amount: 50.00,
                    active: true
                ]
            ]
        ])
    }
    response {
        status OK()
    }
} 
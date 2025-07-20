package contracts.accounts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should add new account successfully when valid session and currency provided"
    
    request {
        method POST()
        url "/api/accounts/addAccount"
        urlPath("/api/accounts/addAccount") {
            queryParameters {
                parameter "sessionId": "test-session-123"
                parameter "currencyCode": "USD"
            }
        }
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
            sessionId: "test-session-123",
            accounts: [
                [
                    id: anyPositiveInt(),
                    currency: [
                        code: "USD",
                        name: "US Dollar"
                    ],
                    amount: 0,
                    active: true
                ]
            ]
        )
    }
} 
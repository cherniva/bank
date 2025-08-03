package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should create a new bank account"
    request {
        method POST()
        url "/cash/sync/create"
        headers {
            contentType(applicationJson())
        }
        body([
            accountId: 1001,
            userDetailsId: 1,
            currencyCode: "USD",
            amount: 100.00,
            active: true
        ])
    }
    response {
        status OK()
    }
} 
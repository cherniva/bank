package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should process a transfer between accounts"
    request {
        method POST()
        url "/cash/sync/transfer"
        headers {
            contentType(applicationJson())
        }
        body([
            sourceAccount: [
                accountId: 1001,
                userDetailsId: 1,
                currencyCode: "USD",
                amount: 75.00,
                active: true
            ],
            destinationAccount: [
                accountId: 1002,
                userDetailsId: 2,
                currencyCode: "USD", 
                amount: 125.00,
                active: true
            ]
        ])
    }
    response {
        status OK()
    }
} 
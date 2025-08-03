package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should transfer amount between accounts"
    request {
        method POST()
        url "/accounts/sync/transfer"
        headers {
            contentType(applicationJson())
        }
        body([
            sourceAccount: [
                accountId: 1001,
                userDetailsId: 1,
                currencyCode: "USD",
                currencyName: "US Dollar",
                amount: 150.00,
                active: true
            ],
            destinationAccount: [
                accountId: 1002,
                userDetailsId: 2,
                currencyCode: "USD",
                currencyName: "US Dollar",
                amount: 250.00,
                active: true
            ]
        ])
    }
    response {
        status OK()
    }
} 
package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should deposit amount to account"
    request {
        method POST()
        url "/accounts/sync/deposit"
        headers {
            contentType(applicationJson())
        }
        body([
            accountId: 1001,
            userDetailsId: 1,
            currencyCode: "USD",
            currencyName: "US Dollar",
            amount: 100.00,
            active: true
        ])
    }
    response {
        status OK()
    }
} 
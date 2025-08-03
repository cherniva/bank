package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return bad request for deposit with invalid account"
    request {
        method POST()
        url "/accounts/sync/deposit"
        headers {
            contentType(applicationJson())
        }
        body([
            accountId: 99999,
            userDetailsId: 1,
            currencyCode: "USD",
            currencyName: "US Dollar",
            amount: 100.00,
            active: true
        ])
    }
    response {
        status BAD_REQUEST()
    }
} 
package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return bad request for withdraw with invalid account"
    request {
        method POST()
        url "/accounts/sync/withdraw"
        headers {
            contentType(applicationJson())
        }
        body([
            accountId: 99999,
            userDetailsId: 1,
            currencyCode: "USD",
            currencyName: "US Dollar",
            amount: 50.00,
            active: true
        ])
    }
    response {
        status BAD_REQUEST()
    }
} 
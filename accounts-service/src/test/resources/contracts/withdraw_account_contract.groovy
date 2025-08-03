package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should withdraw amount from account"
    request {
        method POST()
        url "/accounts/sync/withdraw"
        headers {
            contentType(applicationJson())
        }
        body([
            accountId: 1001,
            userDetailsId: 1,
            currencyCode: "USD",
            currencyName: "US Dollar",
            amount: 50.00,
            active: true
        ])
    }
    response {
        status OK()
    }
} 
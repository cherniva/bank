package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should successfully receive accounts notification"

    request {
        method POST()
        url "/notifications/accounts"
        headers {
            contentType(applicationJson())
        }
        body([
            userId: $(consumer(regex('[a-zA-Z0-9-]+')), producer("user-123")),
            username: $(consumer(anyNonBlankString()), producer("testuser")),
            operationType: $(consumer(anyOf("addAccount", "deposit", "withdraw", "transfer")), producer("deposit")),
            amount: $(consumer(regex('\\d+\\.\\d+')), producer(100.50)),
            currencyCode: $(consumer(regex('[A-Z]{3}')), producer("USD")),
            targetUsername: $(consumer(optional(anyNonBlankString())), producer(null)),
            message: $(consumer(optional(anyNonBlankString())), producer(null))
        ])
    }
    
    response {
        status OK()
    }
} 
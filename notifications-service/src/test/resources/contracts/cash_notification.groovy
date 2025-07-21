package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should successfully receive cash notification"
    
    request {
        method POST()
        url "/notifications/cash"
        headers {
            contentType(applicationJson())
        }
        body([
            userId: $(consumer(regex('[a-zA-Z0-9-]+')), producer("user-123")),
            username: $(consumer(anyNonBlankString()), producer("testuser")),
            operationType: $(consumer(anyOf("deposit", "withdraw")), producer("deposit")),
            amount: $(consumer(regex('\\d+\\.\\d+')), producer(100.50)),
            currencyCode: $(consumer(regex('[A-Z]{3}')), producer("EUR")),
            message: $(consumer(optional(anyNonBlankString())), producer(null))
        ])
    }
    
    response {
        status OK()
    }
} 
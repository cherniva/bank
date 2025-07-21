package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should successfully receive transfer notification"
    
    request {
        method POST()
        url "/notifications/transfer"
        headers {
            contentType(applicationJson())
        }
        body([
            userId: $(consumer(regex('[a-zA-Z0-9-]+')), producer("user-123")),
            username: $(consumer(anyNonBlankString()), producer("testuser")),
            operationType: $(consumer(anyNonBlankString()), producer("transfer")),
            amount: $(consumer(regex('\\d+\\.\\d+')), producer(100.50)),
            fromCurrency: $(consumer(regex('[A-Z]{3}')), producer("USD")),
            convertedAmount: $(consumer(regex('\\d+\\.\\d+')), producer(100.50)),
            toCurrency: $(consumer(regex('[A-Z]{3}')), producer("RUB")),
            targetUsername: $(consumer(anyNonBlankString()), producer("recipient")),
            message: $(consumer(optional(anyNonBlankString())), producer(null))
        ])
    }
    
    response {
        status OK()
    }
} 
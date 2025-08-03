package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return bad request when withdrawing more than account balance"
    request {
        method POST()
        urlPath("/api/cash/withdraw") {
            queryParameters {
                parameter("sessionId", "test-session-123")
                parameter("accountId", "1001")
                parameter("amount", "99999.00") // Amount larger than any reasonable balance
            }
        }
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status BAD_REQUEST()
    }
} 
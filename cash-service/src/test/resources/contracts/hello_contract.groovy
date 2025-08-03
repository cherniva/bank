package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return hello message from cash service"
    request {
        method GET()
        url "/cash/sync"
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status OK()
        body("Hello from cash")
        headers {
            contentType(textPlain())
        }
    }
} 
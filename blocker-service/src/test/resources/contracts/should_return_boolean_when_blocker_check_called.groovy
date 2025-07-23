package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return boolean when blocker check is called"
    
    request {
        method GET()
        url "/blocker/check"
        headers {
            accept(applicationJson())
        }
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(anyBoolean())
    }
} 
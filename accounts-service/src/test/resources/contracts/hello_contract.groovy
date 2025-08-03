package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return hello message from accounts service"
    request {
        method GET()
        url "/api/users/hello"
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status OK()
        body("Hello other service")
        headers {
            contentType(textPlain())
        }
    }
} 
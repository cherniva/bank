package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return bad request when birthdate makes user younger than 18"
    request {
        method POST()
        url "/api/users/editUser"
        urlPath("/api/users/editUser") {
            queryParameters {
                parameter 'sessionId': 'test-session-123'
                parameter 'birthdate': '2010-01-01'
            }
        }
    }
    response {
        status BAD_REQUEST()
    }
} 
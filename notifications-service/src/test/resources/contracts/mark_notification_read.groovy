package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should mark notification as read"
    
    request {
        method POST()
        url $(consumer(regex('/notifications/mark-read/[a-zA-Z0-9-]+')), producer("/notifications/mark-read/notification-123"))
    }
    
    response {
        status OK()
    }
} 
package com.cherniva.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrinterController {

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API Gateway is working!");
    }
}

package com.cherniva.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrinterController {

    @GetMapping("/test")
    public String test() {
        return "API Gateway is working!";
    }
}

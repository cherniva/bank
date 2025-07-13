//package com.cherniva.apigateway.controller;
//
//import com.cherniva.apigateway.config.GatewayConfigProperties;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/texts")
//@RefreshScope
//public class PrinterController {
//
//    private final GatewayConfigProperties config;
//
//    public PrinterController(GatewayConfigProperties config) {
//        this.config = config;
//    }
//
//    @GetMapping
//    public String getText() {
//        return config.getRoutes().get(0).getId(); // or uri, etc.
//    }
//}

package com.cherniva.accountsservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountsController {
    @GetMapping("/api/hello")
    public String hello() {
        return "Hello other service";
    }
}

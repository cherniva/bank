package com.cherniva.accountsservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountsController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello other service";
    }


}

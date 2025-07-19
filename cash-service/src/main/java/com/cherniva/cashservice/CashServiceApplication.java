package com.cherniva.cashservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CashServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CashServiceApplication.class, args);
    }
}

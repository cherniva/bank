package com.cherniva.accountsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.cherniva.common.model"})
@EnableJpaRepositories(basePackages = {"com.cherniva.common.repository"})
public class AccountsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountsServiceApplication.class, args);
    }
}

package com.cherniva.common.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@SpringBootConfiguration
@EnableAutoConfiguration
@ActiveProfiles("test")
@EntityScan(basePackages = "com.cherniva.common.model")
@EnableJpaRepositories(basePackages = "com.cherniva.common.repo")
public class CommonTestConfiguration {
    // Test configuration for common module repository tests
} 
package com.cherniva.frontui.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude = {
    JpaRepositoriesAutoConfiguration.class
})
public class JpaConfig {
    // This configuration disables JPA repository scanning
    // so that the front-ui service doesn't try to connect to a database
} 
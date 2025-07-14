package com.cherniva.accountsservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/api/hello").hasAuthority("SCOPE_resource.read") //Require scope
                                .anyRequest().authenticated() // All other requests need authentication
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt() // Enable JWT-based authentication for the resource server
                ).csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // Disable CSRF for H2 console
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow iframe for H2 console
                );
        return http.build();
    }
}

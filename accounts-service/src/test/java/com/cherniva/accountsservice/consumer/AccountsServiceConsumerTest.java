package com.cherniva.accountsservice.consumer;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest(httpPort = 8089)
public class AccountsServiceConsumerTest { // todo this test chould be in another service

    private final RestTemplate restTemplate = new RestTemplateBuilder().build();
    private final String baseUrl = "http://localhost:8089";

    @Test
    public void should_login_successfully_with_valid_credentials() {
        // Arrange - Setup WireMock stub based on contract
        stubFor(post(urlEqualTo("/api/auth/login"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("{\"username\":\"testuser\",\"password\":\"password123\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" +
                                "\"userId\":1," +
                                "\"username\":\"testuser\"," +
                                "\"name\":\"John\"," +
                                "\"surname\":\"Doe\"," +
                                "\"sessionId\":\"test-session-123\"" +
                                "}")));

        // Act - Make request to accounts service
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> loginRequest = Map.of(
                "username", "testuser",
                "password", "password123"
        );
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/auth/login",
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Assert - Verify response matches contract
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("userId")).isEqualTo(1);
        assertThat(response.getBody().get("username")).isEqualTo("testuser");
        assertThat(response.getBody().get("sessionId")).isEqualTo("test-session-123");
    }

    @Test
    public void should_fail_login_with_invalid_credentials() {
        // Arrange
        stubFor(post(urlEqualTo("/api/auth/login"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("{\"username\":\"wronguser\",\"password\":\"wrongpassword\"}"))
                .willReturn(aResponse()
                        .withStatus(400)));

        // Act & Assert
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> loginRequest = Map.of(
                "username", "wronguser",
                "password", "wrongpassword"
        );
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);
        
        // Expect 400 Bad Request
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                    baseUrl + "/api/auth/login",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
        });
        
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void should_validate_valid_session() {
        // Arrange
        stubFor(post(urlPathEqualTo("/api/auth/validate-session"))
                .withQueryParam("sessionId", equalTo("test-session-123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" +
                                "\"sessionId\":\"test-session-123\"," +
                                "\"valid\":true," +
                                "\"userData\":{" +
                                "\"userId\":1," +
                                "\"username\":\"testuser\"," +
                                "\"name\":\"John\"," +
                                "\"surname\":\"Doe\"" +
                                "}" +
                                "}")));

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/auth/validate-session?sessionId=test-session-123",
                HttpMethod.POST,
                null,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("valid")).isEqualTo(true);
        assertThat(response.getBody().get("sessionId")).isEqualTo("test-session-123");
    }

    @Test
    public void should_reject_underage_user_registration() {
        // Arrange
        stubFor(post(urlEqualTo("/api/accounts/register"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("{" +
                        "\"username\":\"younguser\"," +
                        "\"password\":\"password123\"," +
                        "\"name\":\"Young\"," +
                        "\"surname\":\"User\"," +
                        "\"birthdate\":\"2010-01-01\"" +
                        "}"))
                .willReturn(aResponse()
                        .withStatus(400)));

        // Act & Assert
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> registrationRequest = Map.of(
                "username", "younguser",
                "password", "password123",
                "name", "Young",
                "surname", "User",
                "birthdate", "2010-01-01"
        );
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(registrationRequest, headers);
        
        // Expect 400 Bad Request for underage user
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                    baseUrl + "/api/accounts/register",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
        });
        
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
} 
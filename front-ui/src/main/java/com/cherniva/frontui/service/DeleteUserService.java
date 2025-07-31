package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserRegistrationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteUserService {
    private final RestTemplate restTemplate;

    public void deleteUser(String sessionId) {
        try {
            log.info("delete user");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/users/delete")
                    .queryParam("sessionId", sessionId);

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

            restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Deletion failed", e);
        }
    }
}

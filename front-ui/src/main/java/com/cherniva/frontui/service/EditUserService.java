package com.cherniva.frontui.service;

import com.cherniva.common.dto.UserAccountResponseDto;
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
public class EditUserService {
    private final RestTemplate restTemplate;

    public UserAccountResponseDto editUser(String sessionId, String name, String surname, String birthdate) {
        try {
            log.info("Editing user with sessionId: {}, name: {}, surname: {}, birthdate: {}", sessionId, name, surname, birthdate);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("lb://api-gateway/api/users/editUser")
                    .queryParam("sessionId", sessionId);
            
            // Add optional parameters only if they are not null or empty
            if (name != null && !name.trim().isEmpty()) {
                builder.queryParam("name", name.trim());
            }
            if (surname != null && !surname.trim().isEmpty()) {
                builder.queryParam("surname", surname.trim());
            }
            if (birthdate != null && !birthdate.trim().isEmpty()) {
                builder.queryParam("birthdate", birthdate.trim());
            }

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<UserAccountResponseDto> requestEntity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    requestEntity,
                    UserAccountResponseDto.class)
                    .getBody();
        } catch (Exception e) {
            log.error("Error during user edit operation", e);
            throw new RuntimeException("User edit operation failed", e);
        }
    }
} 
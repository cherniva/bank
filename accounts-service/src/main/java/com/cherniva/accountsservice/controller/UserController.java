package com.cherniva.accountsservice.controller;

import com.cherniva.accountsservice.service.NotificationService;
import com.cherniva.accountsservice.service.SessionService;
import com.cherniva.accountsservice.service.SyncService;
import com.cherniva.accountsservice.utils.SeqGenerator;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserRegistrationDto;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserMapper userMapper;
    private final UserDetailsRepo userDetailsRepo;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final NotificationService notificationService;
    private final SyncService syncService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello other service";
    }

    @PostMapping("/register")
    public ResponseEntity<UserAccountResponseDto> registerUser(@RequestBody UserRegistrationDto userRegistrationDto) {
        try {
            UserDetails userDetails = userMapper.userRegistrationToUser(userRegistrationDto);
            userDetails.setId(SeqGenerator.getNextUserDetails());
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            UserDetails savedUser = userDetailsRepo.save(userDetails);
            syncService.syncUserCreation(savedUser);
            UserAccountResponseDto userAccountResponseDto = userMapper.userToUserAccountResponse(savedUser);
            return ResponseEntity.ok(userAccountResponseDto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/editPassword")
    public ResponseEntity<UserAccountResponseDto> editPassword(@RequestParam String sessionId, @RequestParam String password) {
        try {
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();
            userDetails.setPassword(passwordEncoder.encode(password));
            UserDetails savedUser = userDetailsRepo.save(userDetails);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(savedUser);
            sessionService.removeSession(sessionId);
            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/editUser")
    public ResponseEntity<UserAccountResponseDto> editUser(@RequestParam String sessionId,
                                                           @RequestParam(required = false) String name,
                                                           @RequestParam(required = false) String surname,
                                                           @RequestParam(required = false) String birthdate) {
        try {
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();

            // Track what fields are being updated
            boolean nameUpdated = false;
            boolean surnameUpdated = false;
            boolean birthdateUpdated = false;

            // Update name if provided
            if (name != null && !name.trim().isEmpty()) {
                userDetails.setName(name.trim());
                nameUpdated = true;
            }

            // Update surname if provided
            if (surname != null && !surname.trim().isEmpty()) {
                userDetails.setSurname(surname.trim());
                surnameUpdated = true;
            }

            // Update birthdate if provided
            if (birthdate != null && !birthdate.trim().isEmpty()) {
                try {
                    java.time.LocalDate birthDate = java.time.LocalDate.parse(birthdate);
                    java.time.LocalDate eighteenYearsAgo = java.time.LocalDate.now().minusYears(18);

                    if (birthDate.isAfter(eighteenYearsAgo)) {
                        log.error("User attempted to set birthdate that makes them younger than 18: {}", birthdate);

                        // Send notification for failed operation
                        notificationService.sendEditUserNotification(
                                userDetails.getId().toString(),
                                userDetails.getUsername(),
                                nameUpdated ? name : null,
                                surnameUpdated ? surname : null,
                                birthdate,
                                false // failed
                        );

                        return ResponseEntity.badRequest().build();
                    }

                    userDetails.setBirthdate(birthDate);
                    birthdateUpdated = true;
                } catch (Exception e) {
                    log.error("Invalid birthdate format: {}", birthdate, e);

                    // Send notification for failed operation
                    notificationService.sendEditUserNotification(
                            userDetails.getId().toString(),
                            userDetails.getUsername(),
                            nameUpdated ? name : null,
                            surnameUpdated ? surname : null,
                            birthdate,
                            false // failed
                    );

                    return ResponseEntity.badRequest().build();
                }
            }

            UserDetails savedUser = userDetailsRepo.save(userDetails);
            UserAccountResponseDto updatedUserAccountResponseDto = userMapper.userToUserAccountResponse(savedUser);

            // Remove old session and create new one
            sessionService.removeSession(sessionId);
            String newSessionId = sessionService.createSession(updatedUserAccountResponseDto);
            updatedUserAccountResponseDto.setSessionId(newSessionId);

            // Send notification for successful operation
            notificationService.sendEditUserNotification(
                    userDetails.getId().toString(),
                    userDetails.getUsername(),
                    nameUpdated ? name : null,
                    surnameUpdated ? surname : null,
                    birthdateUpdated ? birthdate : null,
                    true // success
            );

            return ResponseEntity.ok(updatedUserAccountResponseDto);
        } catch (Exception e) {
            log.error("Edit user operation failed", e);

            // Send notification for failed operation (exception)
            try {
                SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
                if (sessionInfo != null && sessionInfo.getUserData() != null) {
                    notificationService.sendEditUserNotification(
                            sessionInfo.getUserData().getUserId().toString(),
                            sessionInfo.getUserData().getUsername(),
                            name,
                            surname,
                            birthdate,
                            false // failed
                    );
                }
            } catch (Exception notificationException) {
                log.error("Failed to send edit user failure notification", notificationException);
            }

            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam String sessionId) {
        try {
            SessionService.SessionInfo sessionInfo = sessionService.getSession(sessionId);
            UserDetails userDetails = userDetailsRepo.findById(sessionInfo.getUserData().getUserId()).orElseThrow();
            userDetailsRepo.delete(userDetails);
            sessionService.removeSession(sessionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
    }
}

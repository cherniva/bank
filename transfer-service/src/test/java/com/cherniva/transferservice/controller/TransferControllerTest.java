package com.cherniva.transferservice.controller;

import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.transferservice.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

//    @Mock
//    private TransferService transferService;
//
//    @InjectMocks
//    private TransferController transferController;
//
//    private UserAccountResponseDto mockUserResponse;
//
//    @BeforeEach
//    void setUp() {
//        mockUserResponse = new UserAccountResponseDto();
//        mockUserResponse.setUserId(1L);
//        mockUserResponse.setUsername("testuser");
//    }
//
//    @Test
//    void transfer_SuccessfulOperation_ReturnsOkResponse() {
//        // Arrange
//        when(transferService.transfer("session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient"))
//                .thenReturn(mockUserResponse);
//
//        // Act
//        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
//                "session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient");
//
//        // Assert
//        assertEquals(200, response.getStatusCodeValue());
//        assertNotNull(response.getBody());
//        assertEquals("testuser", response.getBody().getUsername());
//        verify(transferService).transfer("session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient");
//    }
//
//    @Test
//    void transfer_BlockedOperation_ReturnsNullResponse() {
//        // Arrange
//        when(transferService.transfer("session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient"))
//                .thenReturn(null);
//
//        // Act
//        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
//                "session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient");
//
//        // Assert
//        assertEquals(404, response.getStatusCodeValue());
//        assertNull(response.getBody());
//        verify(transferService).transfer("session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient");
//    }
//
//    @Test
//    void transfer_ExceptionThrown_ReturnsBadRequest() {
//        // Arrange
//        when(transferService.transfer("session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient"))
//                .thenThrow(new RuntimeException("Service error"));
//
//        // Act
//        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
//                "session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient");
//
//        // Assert
//        assertEquals(400, response.getStatusCodeValue());
//        verify(transferService).transfer("session123", BigDecimal.valueOf(100), "USD", "EUR", "recipient");
//    }
//
//    @Test
//    void transfer_WithNullParameters_HandlesGracefully() {
//        // Arrange
//        when(transferService.transfer(any(), any(), any(), any(), any()))
//                .thenThrow(new RuntimeException("Invalid parameters"));
//
//        // Act
//        ResponseEntity<UserAccountResponseDto> response = transferController.transfer(
//                null, null, null, null, null);
//
//        // Assert
//        assertEquals(400, response.getStatusCodeValue());
//    }
} 
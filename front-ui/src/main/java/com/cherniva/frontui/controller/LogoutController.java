package com.cherniva.frontui.controller;

import com.cherniva.frontui.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/logout")
@RequiredArgsConstructor
public class LogoutController {

    private final SessionService sessionService;

    @GetMapping
    public String logout(@CookieValue(value = "sessionId", required = false) String sessionId,
                        HttpServletResponse response) {
        
        if (sessionId != null) {
            // Call accounts-service to remove session
            sessionService.logout(sessionId);
            
            // Clear the session cookie
            Cookie sessionCookie = new Cookie("sessionId", "");
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(0);
            response.addCookie(sessionCookie);
        }
        
        return "redirect:/login";
    }
} 
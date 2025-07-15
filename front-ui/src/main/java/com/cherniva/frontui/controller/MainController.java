package com.cherniva.frontui.controller;

import com.cherniva.common.dto.SessionValidationDto;
import com.cherniva.frontui.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final SessionService sessionService;

    @GetMapping("/")
    public String home(@CookieValue(value = "sessionId", required = false) String sessionId, Model model) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);
            
            if (sessionValidation.isValid()) {
                // User is authenticated
                model.addAttribute("username", sessionValidation.getUsername());
                model.addAttribute("userId", sessionValidation.getUserId());
                model.addAttribute("authenticated", true);
                return "main";
            }
        }
        
        // User is not authenticated
        model.addAttribute("authenticated", false);
        return "redirect:/login";
    }

    @GetMapping("/main")
    public String mainPage(@CookieValue(value = "sessionId", required = false) String sessionId, Model model) {
        if (sessionId != null) {
            SessionValidationDto sessionValidation = sessionService.validateSession(sessionId);
            
            if (sessionValidation.isValid()) {
                model.addAttribute("username", sessionValidation.getUsername());
                model.addAttribute("userId", sessionValidation.getUserId());
                model.addAttribute("authenticated", true);
                return "main";
            }
        }
        
        return "redirect:/login";
    }
} 
package com.cherniva.frontui.controller;

import com.cherniva.common.dto.UserLoginDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.frontui.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {
    
    private final AuthService authService;
    
    @GetMapping
    public String getSignup(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Registration failed: " + error);
        }
        return "signup";
    }
    
    @PostMapping
    public String processSignup(@RequestParam String username, 
                              @RequestParam String password, 
                              @RequestParam String confirmPassword,
                              @RequestParam String name,
                              @RequestParam String surname,
                              @RequestParam String birthdate,
                              Model model,
                              HttpServletResponse response) {
        
        // Basic validation
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }
        
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username is required");
            return "signup";
        }
        
        if (password == null || password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long");
            return "signup";
        }
        
        try {
            // For now, we'll just redirect to login since we don't have a registration endpoint
            // In a real application, you would call a registration service
            model.addAttribute("success", "Registration successful! Please login.");
            return "redirect:/login?success=true";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "signup";
        }
    }
}

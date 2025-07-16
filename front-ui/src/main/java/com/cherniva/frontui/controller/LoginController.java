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
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {
    
    private final AuthService authService;
    
    @GetMapping
    public String getLogin(@RequestParam(value = "error", required = false) String error, 
                          @RequestParam(value = "success", required = false) String success,
                          Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (success != null) {
            model.addAttribute("success", "Registration successful! Please login with your new account.");
        }
        return "login";
    }
    
    @PostMapping
    public String processLogin(@RequestParam String username, 
                             @RequestParam String password, 
                             Model model,
                             HttpServletResponse response) {
        try {
            UserLoginDto loginDto = new UserLoginDto();
            loginDto.setUsername(username);
            loginDto.setPassword(password);
            
//            UserAccountResponseDto userResponse = authService.authenticateUser(loginDto);
//
//            // Set session cookie
//            Cookie sessionCookie = new Cookie("sessionId", userResponse.getSessionId());
//            sessionCookie.setPath("/");
//            sessionCookie.setMaxAge(30 * 60); // 30 minutes
//            response.addCookie(sessionCookie);
            
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }
}

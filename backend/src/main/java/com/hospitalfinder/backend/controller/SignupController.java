package com.hospitalfinder.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalfinder.backend.dto.LoginResponse;
import com.hospitalfinder.backend.dto.SignupRequest;
import com.hospitalfinder.backend.entity.Role;
import com.hospitalfinder.backend.entity.User;
import com.hospitalfinder.backend.repository.UserRepository;
import com.hospitalfinder.backend.service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class SignupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public SignupController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@RequestBody SignupRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(new LoginResponse(false, "Email already registered", null, null, null, null, null));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        user = userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        setCookies(response, jwtToken, user.getEmail());

        return ResponseEntity.ok(new LoginResponse(
            true,
            "User registered successfully",
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole(),
            jwtToken
        ));
    }
    
    private void setCookies(HttpServletResponse response, String jwtToken, String email) {
        // Create JWT cookie
        Cookie jwtCookie = new Cookie("jwt_token", jwtToken);
        jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setAttribute("SameSite", "None");
        response.addCookie(jwtCookie);

        // Create user info cookie
        Cookie userCookie = new Cookie("user_info", email);
        userCookie.setMaxAge(7 * 24 * 60 * 60);
        userCookie.setPath("/");
        userCookie.setHttpOnly(false);
        userCookie.setSecure(true);
        userCookie.setAttribute("SameSite", "None");
        response.addCookie(userCookie);
    }
}
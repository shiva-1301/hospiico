package com.hospitalfinder.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalfinder.backend.dto.LoginResponse;
import com.hospitalfinder.backend.dto.PartnerSignupRequest;
import com.hospitalfinder.backend.entity.Clinic;
import com.hospitalfinder.backend.entity.Role;
import com.hospitalfinder.backend.entity.User;
import com.hospitalfinder.backend.repository.ClinicRepository;
import com.hospitalfinder.backend.repository.UserRepository;
import com.hospitalfinder.backend.service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/partner")
@RequiredArgsConstructor
public class PartnerSignupController {

    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<LoginResponse> partnerSignup(
            @RequestBody PartnerSignupRequest request,
            HttpServletResponse response) {

        // Validate email not already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(false, "Email already registered", null, null, null, null, null, null));
        }

        // Validate hospital name not already taken
        if (clinicRepository.existsByName(request.getHospitalName())) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(false, "Hospital name already exists", null, null, null, null, null, null));
        }

        // Create user account
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.HOSPITAL);

        // Save user first to get ID
        user = userRepository.save(user);

        // Create hospital/clinic
        Clinic clinic = new Clinic();
        clinic.setName(request.getHospitalName());
        clinic.setAddress(request.getAddress());
        clinic.setCity(request.getCity());
        clinic.setState(request.getState());
        clinic.setPincode(request.getPincode());
        clinic.setPhone(request.getHospitalPhone());
        clinic.setEmail(request.getHospitalEmail());
        clinic.setSpecializations(request.getSpecializations());
        clinic.setDescription(request.getDescription());
        clinic.setLatitude(request.getLatitude());
        clinic.setLongitude(request.getLongitude());
        clinic.setImageUrl(request.getImageUrl());
        clinic.setOwnerId(user.getId());
        clinic.setRating(0.0); // Default rating

        // Save clinic
        clinic = clinicRepository.save(clinic);

        // Link hospital to user
        user.setHospitalId(clinic.getId());
        user = userRepository.save(user);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);
        setCookies(response, jwtToken, user.getEmail());

        return ResponseEntity.ok(new LoginResponse(
                true,
                "Partner account created successfully",
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getDoctorId(),
                jwtToken));
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

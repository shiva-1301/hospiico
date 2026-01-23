package com.hospitalfinder.backend.dto;

import com.hospitalfinder.backend.entity.Role;
import lombok.*;

public class LoginResponse {
    @Getter
    @Setter
    private boolean success;
    @Getter
    @Setter
    private String message;
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Role role;
    @Getter
    @Setter
    private String doctorId; // Added for doctor dashboard
    @Getter
    @Setter
    private String token; // for the cookie value reference

    public LoginResponse(boolean success, String message, String id, String email, String name, Role role,
            String doctorId, String token) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.doctorId = doctorId;
        this.token = token;
    }
}

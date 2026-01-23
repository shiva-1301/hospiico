package com.hospitalfinder.backend.dto;

import java.util.List;

import com.hospitalfinder.backend.entity.Specialization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSignupRequest {

    // User details
    private String name;
    private String email;
    private String password;
    private String phone;

    // Hospital details
    private String hospitalName;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String hospitalPhone;
    private String hospitalEmail;
    private List<Specialization> specializations;
    private String description;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
}

package com.hospitalfinder.backend.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "clinics")
public class Clinic {
    @Id
    @Getter
    @Setter
    private String id;

    // Owner of this hospital (User with HOSPITAL role)
    @Getter
    @Setter
    private String ownerId;

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String address;
    @Getter
    @Setter
    private String city;
    @Getter
    @Setter
    private String state;
    @Getter
    @Setter
    private String pincode;
    @Getter
    @Setter
    private Double latitude;
    @Getter
    @Setter
    private Double longitude;
    @Getter
    @Setter
    private Collection<Specialization> specializations = new ArrayList<>();
    @Getter
    @Setter
    private String phone;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String website;
    @Getter
    @Setter
    private String timings;
    @Getter
    @Setter
    private Double rating;
    @Getter
    @Setter
    private Integer reviews;
    @Getter
    @Setter
    private List<Doctor> doctors = new ArrayList<>();
    @Getter
    @Setter
    private String imageUrl;
}
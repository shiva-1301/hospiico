package com.hospitalfinder.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "doctors")
public class Doctor {
    @Id
    @Getter @Setter
    private String id;

    @Getter @Setter
    private String name;
    @Getter @Setter
    private String qualifications;
    @Getter @Setter
    private String specialization;
    @Getter @Setter
    private String experience;
    @Getter @Setter
    private String biography;
    @Getter @Setter
    private String clinicId;
    @Getter @Setter
    private String imageUrl;
}
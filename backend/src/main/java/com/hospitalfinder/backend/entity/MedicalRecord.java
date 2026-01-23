package com.hospitalfinder.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Document(collection = "medical_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    private String id;

    private String name;

    private String type;

    private long size;

    private String category; // Diagnostics, Scanning, Prescriptions, Bills

    @JsonIgnore
    private byte[] data;

    private LocalDateTime uploadDate;

    // Link to User
    private String userId;
}

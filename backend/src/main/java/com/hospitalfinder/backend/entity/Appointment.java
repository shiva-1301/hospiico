package com.hospitalfinder.backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "appointments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Appointment {

    @Id
    private String id;

    // the person who is booking the appointment
    private String userId;

    private String clinicId;

    private String doctorId;

    private LocalDateTime appointmentTime;

    private String status; // BOOKED, CANCELLED

    // NEW: patient details (for someone else)
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private String patientPhone;
    private String patientEmail;
    private String reason;
}

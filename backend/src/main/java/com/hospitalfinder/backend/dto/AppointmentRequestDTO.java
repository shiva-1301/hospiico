package com.hospitalfinder.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AppointmentRequestDTO {

    private String userId;
    private String clinicId;
    private String doctorId;

    private String appointmentTime;  // ISO format

    // patient details
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private String patientPhone;
    private String patientEmail;
    private String reason;
}

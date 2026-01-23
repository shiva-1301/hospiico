package com.hospitalfinder.backend.dto;

import com.hospitalfinder.backend.entity.Appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentResponseDTO {

    private String id;
    private String clinicId;
    private String clinicName;
    private String doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String userId;
    private String userName;
    private String appointmentTime;
    private String status;

    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private String patientEmail;
    private String patientPhone;
    private String reason;

    public AppointmentResponseDTO(Appointment appointment) {
        this.id = appointment.getId();
        this.clinicId = appointment.getClinicId();
        this.clinicName = ""; // Will be fetched by service
        this.doctorId = appointment.getDoctorId();
        this.doctorName = ""; // Will be fetched by service
        this.doctorSpecialization = ""; // Will be fetched by service
        this.userId = appointment.getUserId();
        this.userName = ""; // Will be fetched by service
        this.appointmentTime = appointment.getAppointmentTime().toString();
        this.status = appointment.getStatus();

        this.patientName = appointment.getPatientName();
        this.patientAge = appointment.getPatientAge();
        this.patientGender = appointment.getPatientGender();
        this.patientEmail = appointment.getPatientEmail();
        this.patientPhone = appointment.getPatientPhone();
        this.reason = appointment.getReason();
    }
}

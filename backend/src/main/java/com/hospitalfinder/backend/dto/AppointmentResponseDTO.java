package com.hospitalfinder.backend.dto;

import com.hospitalfinder.backend.entity.Appointment;
import com.hospitalfinder.backend.entity.Clinic;
import com.hospitalfinder.backend.entity.Doctor;
import com.hospitalfinder.backend.entity.User;

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
        this.clinicName = appointment.getClinicId(); // Default to ID, will be overridden by service
        this.doctorId = appointment.getDoctorId();
        this.doctorName = appointment.getDoctorId(); // Default to ID, will be overridden by service
        this.doctorSpecialization = ""; // Will be fetched by service
        this.userId = appointment.getUserId();
        this.userName = appointment.getUserId() != null ? appointment.getUserId() : ""; // Will be overridden by service
        this.appointmentTime = appointment.getAppointmentTime().toString();
        this.status = appointment.getStatus();

        this.patientName = appointment.getPatientName();
        this.patientAge = appointment.getPatientAge();
        this.patientGender = appointment.getPatientGender();
        this.patientEmail = appointment.getPatientEmail();
        this.patientPhone = appointment.getPatientPhone();
        this.reason = appointment.getReason();
    }

    // Constructor with enriched data
    public AppointmentResponseDTO(Appointment appointment, Clinic clinic, Doctor doctor, User user) {
        this.id = appointment.getId();
        this.clinicId = appointment.getClinicId();
        this.clinicName = clinic != null ? clinic.getName() : appointment.getClinicId();
        this.doctorId = appointment.getDoctorId();
        this.doctorName = doctor != null ? doctor.getName() : appointment.getDoctorId();
        this.doctorSpecialization = doctor != null ? doctor.getSpecialization() : "";
        this.userId = appointment.getUserId();
        this.userName = user != null ? user.getName() : (appointment.getUserId() != null ? appointment.getUserId() : "");
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

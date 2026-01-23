package com.hospitalfinder.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalfinder.backend.dto.AppointmentResponseDTO;
import com.hospitalfinder.backend.entity.Appointment;
import com.hospitalfinder.backend.entity.Clinic;
import com.hospitalfinder.backend.entity.Role;
import com.hospitalfinder.backend.entity.User;
import com.hospitalfinder.backend.repository.AppointmentRepository;
import com.hospitalfinder.backend.repository.ClinicRepository;
import com.hospitalfinder.backend.repository.UserRepository;
import com.hospitalfinder.backend.service.JwtService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Get all appointments - ADMIN only
     */
    @GetMapping("/appointments/all")
    public ResponseEntity<?> getAllAppointments() {
        try {
            User currentUser = getCurrentUser();

            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            if (currentUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(403).body("Access denied. Admin only.");
            }

            List<Appointment> appointments = appointmentRepository.findAll();
            var responseList = appointments.stream()
                    .map(AppointmentResponseDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get appointments for hospital owner's hospital - HOSPITAL role only
     */
    @GetMapping("/appointments/my-hospital")
    public ResponseEntity<?> getMyHospitalAppointments(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            User currentUser = getCurrentUserFromToken(authorization);

            if (currentUser == null) {
                System.err.println("❌ No user found from token");
                return ResponseEntity.status(401).body("Unauthorized - Please login again");
            }

            System.out.println("✅ Current user: " + currentUser.getEmail() + ", Role: " + currentUser.getRole());

            if (currentUser.getRole() != Role.HOSPITAL) {
                System.err.println("❌ User role mismatch. Expected HOSPITAL, got: " + currentUser.getRole());
                return ResponseEntity.status(403)
                        .body("Access denied. Hospital owner only. Your role: " + currentUser.getRole());
            }

            if (currentUser.getHospitalId() == null) {
                System.err.println("❌ User has no hospitalId assigned");
                return ResponseEntity.badRequest().body("No hospital associated with this account");
            }

            System.out.println("✅ Fetching appointments for hospital: " + currentUser.getHospitalId());

            List<Appointment> appointments = appointmentRepository.findByClinicId(currentUser.getHospitalId())
                    .stream()
                    .collect(Collectors.toList());

            System.out.println("✅ Found " + appointments.size() + " appointments");

            var responseList = appointments.stream()
                    .map(AppointmentResponseDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            System.err.println("❌ Error in getMyHospitalAppointments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get appointments for doctor's patients - DOCTOR role only
     */
    @GetMapping("/appointments/my-patients")
    public ResponseEntity<?> getMyPatientAppointments(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            User currentUser = getCurrentUserFromToken(authorization);

            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            if (currentUser.getRole() != Role.DOCTOR) {
                return ResponseEntity.status(403).body("Access denied. Doctor only.");
            }

            if (currentUser.getDoctorId() == null) {
                return ResponseEntity.badRequest().body("No doctor profile associated with this account");
            }

            List<Appointment> appointments = appointmentRepository.findByDoctorId(currentUser.getDoctorId());
            var responseList = appointments.stream()
                    .map(AppointmentResponseDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get dashboard statistics based on user role
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            User currentUser = getCurrentUserFromToken(authorization);

            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            if (currentUser.getRole() == Role.ADMIN) {
                return getAdminStats();
            } else if (currentUser.getRole() == Role.HOSPITAL) {
                return getHospitalStats(currentUser);
            } else if (currentUser.getRole() == Role.DOCTOR) {
                return getDoctorStats(currentUser);
            } else {
                return ResponseEntity.status(403).body("No dashboard available for this role");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private ResponseEntity<?> getAdminStats() {
        long totalHospitals = clinicRepository.count();
        long totalAppointments = appointmentRepository.count();
        long totalUsers = userRepository.count();

        return ResponseEntity.ok(new DashboardStats(
                totalHospitals,
                totalAppointments,
                totalUsers,
                0L));
    }

    private ResponseEntity<?> getHospitalStats(User user) {
        if (user.getHospitalId() == null) {
            return ResponseEntity.badRequest().body("No hospital associated with this account");
        }

        long appointmentCount = appointmentRepository.findByClinicId(user.getHospitalId()).size();

        Clinic clinic = clinicRepository.findById(user.getHospitalId()).orElse(null);
        long doctorCount = clinic != null ? clinic.getDoctors().size() : 0;

        return ResponseEntity.ok(new DashboardStats(
                1L,
                appointmentCount,
                0L,
                doctorCount));
    }

    private ResponseEntity<?> getDoctorStats(User user) {
        if (user.getDoctorId() == null) {
            return ResponseEntity.badRequest().body("No doctor profile associated with this account");
        }

        long appointmentCount = appointmentRepository.findByDoctorId(user.getDoctorId()).size();

        return ResponseEntity.ok(new DashboardStats(
                0L,
                appointmentCount,
                0L,
                0L));
    }

    private User getCurrentUserFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.err.println("❌ No Authorization header or invalid format");
            return null;
        }

        String token = authorization.substring(7);

        try {
            String email = jwtService.extractUsername(token);
            System.out.println("✅ Extracted email from JWT: " + email);

            User user = userRepository.findByEmail(email);
            if (user == null) {
                System.err.println("❌ User not found for email: " + email);
            }
            return user;
        } catch (Exception e) {
            System.err.println("❌ Error extracting user from token: " + e.getMessage());
            return null;
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

    // Inner class for dashboard statistics response
    public static class DashboardStats {
        private long hospitalCount;
        private long appointmentCount;
        private long userCount;
        private long doctorCount;

        public DashboardStats(long hospitalCount, long appointmentCount, long userCount, long doctorCount) {
            this.hospitalCount = hospitalCount;
            this.appointmentCount = appointmentCount;
            this.userCount = userCount;
            this.doctorCount = doctorCount;
        }

        public long getHospitalCount() {
            return hospitalCount;
        }

        public long getAppointmentCount() {
            return appointmentCount;
        }

        public long getUserCount() {
            return userCount;
        }

        public long getDoctorCount() {
            return doctorCount;
        }
    }
}

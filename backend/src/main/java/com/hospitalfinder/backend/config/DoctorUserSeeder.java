package com.hospitalfinder.backend.config;

import com.hospitalfinder.backend.entity.Doctor;
import com.hospitalfinder.backend.entity.Role;
import com.hospitalfinder.backend.entity.User;
import com.hospitalfinder.backend.repository.DoctorRepository;
import com.hospitalfinder.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(10) // Run after other seeders
@RequiredArgsConstructor
public class DoctorUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedDoctorUser();
    }

    private void seedDoctorUser() {
        // Check if doctor user already exists
        User existingDoctor = userRepository.findByEmail("dr.sarah@cityhospital.com");
        if (existingDoctor != null) {
            System.out.println("✅ Doctor user already exists - skipping");
            return;
        }

        // Find a doctor from the database to link to
        List<Doctor> doctors = doctorRepository.findAll();
        if (doctors.isEmpty()) {
            System.out.println("❌ No doctors found in database. Please seed doctors first.");
            return;
        }

        // Get the first doctor (or you can search for a specific one)
        Doctor doctor = doctors.get(0);

        // Create doctor user
        User doctorUser = new User();
        doctorUser.setName(doctor.getName());
        doctorUser.setEmail("dr.sarah@cityhospital.com");
        doctorUser.setPassword(passwordEncoder.encode("doctor123")); // Password: doctor123
        doctorUser.setPhone("9876543220");
        doctorUser.setRole(Role.DOCTOR);
        doctorUser.setDoctorId(doctor.getId()); // Link to doctor profile
        doctorUser.setAge(35);
        doctorUser.setGender("Female");

        userRepository.save(doctorUser);

        System.out.println("✅ Created DOCTOR user:");
        System.out.println("   Email: dr.sarah@cityhospital.com");
        System.out.println("   Password: doctor123");
        System.out.println("   Name: " + doctor.getName());
        System.out.println("   Doctor ID: " + doctor.getId());
        System.out.println("   Specialization: " + doctor.getSpecialization());
        System.out.println("   Clinic ID: " + doctor.getClinicId());
    }
}

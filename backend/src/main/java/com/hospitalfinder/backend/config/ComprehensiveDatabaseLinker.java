package com.hospitalfinder.backend.config;

import com.hospitalfinder.backend.entity.Clinic;
import com.hospitalfinder.backend.entity.Doctor;
import com.hospitalfinder.backend.entity.Role;
import com.hospitalfinder.backend.entity.User;
import com.hospitalfinder.backend.repository.ClinicRepository;
import com.hospitalfinder.backend.repository.DoctorRepository;
import com.hospitalfinder.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Component
@Order(15) // Run after all other seeders
@RequiredArgsConstructor
public class ComprehensiveDatabaseLinker implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    private final StringBuilder reportBuilder = new StringBuilder();

    @Override
    public void run(String... args) {
        System.out.println("\n========================================");
        System.out.println("🔗 COMPREHENSIVE DATABASE LINKER STARTED");
        System.out.println("========================================\n");

        reportBuilder.append("# 🏥 HOSPICO DATABASE - COMPLETE CREDENTIALS REPORT\n\n");
        reportBuilder.append("**Generated:** ").append(new Date()).append("\n\n");
        reportBuilder.append("---\n\n");

        try {
            // Step 1: Link all hospitals to owners
            linkHospitalsToOwners();

            // Step 2: Ensure all doctors are assigned to exactly one hospital
            assignDoctorsToHospitals();

            // Step 3: Create user accounts for all doctors
            createDoctorUserAccounts();

            // Step 4: Generate comprehensive report
            generateFinalReport();

            // Step 5: Save report to file
            saveReportToFile();

            System.out.println("\n========================================");
            System.out.println("✅ DATABASE LINKING COMPLETED SUCCESSFULLY!");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("❌ Error during database linking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void linkHospitalsToOwners() {
        System.out.println("📋 Step 1: Linking hospitals to owners...\n");

        List<Clinic> allClinics = clinicRepository.findAll();
        List<Clinic> unownedClinics = new ArrayList<>();

        // Find clinics without owners
        for (Clinic clinic : allClinics) {
            if (clinic.getOwnerId() == null || clinic.getOwnerId().isEmpty()) {
                unownedClinics.add(clinic);
            }
        }

        System.out.println("   Found " + unownedClinics.size() + " hospitals without owners");

        // Create owners for unowned clinics
        String[] ownerNames = {
                "Dr. Ramesh Reddy", "Dr. Priya Sharma", "Dr. Vikram Gupta",
                "Dr. Anita Rao", "Dr. Suresh Kumar", "Dr. Lakshmi Nair",
                "Dr. Rajesh Mehta", "Dr. Kavita Singh", "Dr. Manoj Patel"
        };

        int ownerIndex = 0;
        for (Clinic clinic : unownedClinics) {
            // Create a new hospital owner
            User owner = new User();
            String ownerName = ownerNames[ownerIndex % ownerNames.length];
            String email = ownerName.toLowerCase().replace("dr. ", "").replace(" ", ".") + "@" +
                    clinic.getName().toLowerCase().replace(" ", "").substring(0,
                            Math.min(10, clinic.getName().length()))
                    + ".com";

            owner.setName(ownerName);
            owner.setEmail(email);
            owner.setPassword(passwordEncoder.encode("hospital123"));
            owner.setPhone("98765432" + String.format("%02d", 10 + ownerIndex));
            owner.setRole(Role.HOSPITAL);
            owner.setHospitalId(clinic.getId());

            owner = userRepository.save(owner);

            // Link clinic to owner
            clinic.setOwnerId(owner.getId());
            clinicRepository.save(clinic);

            System.out.println("   ✅ Created owner: " + ownerName + " for " + clinic.getName());

            ownerIndex++;
        }

        System.out.println("\n   ✅ All hospitals now have owners!\n");
    }

    private void assignDoctorsToHospitals() {
        System.out.println("📋 Step 2: Assigning doctors to hospitals...\n");

        List<Doctor> allDoctors = doctorRepository.findAll();
        List<Clinic> allClinics = clinicRepository.findAll();

        // Track doctors per clinic to ensure no duplicates
        Map<String, Set<String>> clinicDoctors = new HashMap<>();
        int reassigned = 0;

        for (Doctor doctor : allDoctors) {
            if (doctor.getClinicId() == null || doctor.getClinicId().isEmpty()) {
                // Assign to a random clinic
                Clinic randomClinic = allClinics.get(new Random().nextInt(allClinics.size()));
                doctor.setClinicId(randomClinic.getId());
                doctorRepository.save(doctor);
                reassigned++;
            }

            // Track assignments
            clinicDoctors.computeIfAbsent(doctor.getClinicId(), k -> new HashSet<>()).add(doctor.getId());
        }

        System.out.println("   ✅ Assigned " + reassigned + " unassigned doctors to hospitals");
        System.out.println("   ✅ Total doctors: " + allDoctors.size() + "\n");
    }

    private void createDoctorUserAccounts() {
        System.out.println("📋 Step 3: Creating user accounts for all doctors...\n");

        List<Doctor> allDoctors = doctorRepository.findAll();
        int created = 0;

        for (Doctor doctor : allDoctors) {
            // Create email from doctor name
            String email = "dr." + doctor.getName().toLowerCase()
                    .replace("dr. ", "")
                    .replace(" ", ".")
                    .replaceAll("[^a-z.]", "") + "@hospico.com";

            // Check if user already exists
            User existingUser = userRepository.findByEmail(email);
            if (existingUser != null) {
                // Update doctorId if not set
                if (existingUser.getDoctorId() == null) {
                    existingUser.setDoctorId(doctor.getId());
                    userRepository.save(existingUser);
                }
                continue;
            }

            // Create new doctor user
            User doctorUser = new User();
            doctorUser.setName(doctor.getName());
            doctorUser.setEmail(email);
            doctorUser.setPassword(passwordEncoder.encode("doctor123"));
            doctorUser.setPhone("91234567" + String.format("%02d", created % 100));
            doctorUser.setRole(Role.DOCTOR);
            doctorUser.setDoctorId(doctor.getId());
            doctorUser.setAge(35 + (created % 20));
            doctorUser.setGender(created % 2 == 0 ? "Male" : "Female");

            userRepository.save(doctorUser);
            created++;

            System.out.println("   ✅ Created doctor account: " + email);
        }

        System.out.println("\n   ✅ Created " + created + " doctor user accounts\n");
    }

    private void generateFinalReport() {
        System.out.println("📋 Step 4: Generating comprehensive report...\n");

        // Section 1: All Hospitals and Their Owners
        reportBuilder.append("## 🏥 ALL HOSPITALS & OWNERS\n\n");

        List<Clinic> allClinics = clinicRepository.findAll();
        int hospitalNumber = 1;

        for (Clinic clinic : allClinics) {
            User owner = null;
            if (clinic.getOwnerId() != null) {
                owner = userRepository.findById(clinic.getOwnerId()).orElse(null);
            }

            reportBuilder.append("### ").append(hospitalNumber++).append(". ").append(clinic.getName()).append("\n");
            reportBuilder.append("- **Hospital ID:** `").append(clinic.getId()).append("`\n");
            reportBuilder.append("- **Location:** ").append(clinic.getCity()).append(", ").append(clinic.getAddress())
                    .append("\n");

            if (owner != null) {
                reportBuilder.append("- **Owner:** ").append(owner.getName()).append("\n");
                reportBuilder.append("  - **Owner Email:** `").append(owner.getEmail()).append("`\n");
                reportBuilder.append("  - **Owner Password:** `hospital123`\n");
                reportBuilder.append("  - **Owner Phone:** ").append(owner.getPhone()).append("\n");
            }

            // Count doctors in this clinic
            List<Doctor> clinicDoctors = doctorRepository.findByClinicId(clinic.getId());
            reportBuilder.append("- **Number of Doctors:** ").append(clinicDoctors.size()).append("\n\n");
        }

        reportBuilder.append("---\n\n");

        // Section 2: All Doctors
        reportBuilder.append("## 👨‍⚕️ ALL DOCTORS & CREDENTIALS\n\n");

        List<Doctor> allDoctors = doctorRepository.findAll();
        int doctorNumber = 1;

        for (Doctor doctor : allDoctors) {
            Clinic clinic = clinicRepository.findById(doctor.getClinicId()).orElse(null);

            // Find doctor's user account
            List<User> doctorUsers = userRepository.findAll().stream()
                    .filter(u -> doctor.getId().equals(u.getDoctorId()))
                    .toList();

            reportBuilder.append("### ").append(doctorNumber++).append(". ").append(doctor.getName()).append("\n");
            reportBuilder.append("- **Doctor ID:** `").append(doctor.getId()).append("`\n");
            reportBuilder.append("- **Specialization:** ").append(doctor.getSpecialization()).append("\n");
            reportBuilder.append("- **Qualifications:** ")
                    .append(doctor.getQualifications() != null ? doctor.getQualifications() : "MBBS, MD").append("\n");

            if (clinic != null) {
                reportBuilder.append("- **Works At:** ").append(clinic.getName()).append("\n");
                reportBuilder.append("- **Clinic ID:** `").append(clinic.getId()).append("`\n");
            }

            if (!doctorUsers.isEmpty()) {
                User doctorUser = doctorUsers.get(0);
                reportBuilder.append("- **Login Email:** `").append(doctorUser.getEmail()).append("`\n");
                reportBuilder.append("- **Login Password:** `doctor123`\n");
                reportBuilder.append("- **Phone:** ").append(doctorUser.getPhone()).append("\n");
            }

            reportBuilder.append("\n");
        }

        reportBuilder.append("---\n\n");

        // Section 3: All User Accounts Summary
        reportBuilder.append("## 👥 ALL USER ACCOUNTS SUMMARY\n\n");

        List<User> allUsers = userRepository.findAll();

        Map<Role, List<User>> usersByRole = new HashMap<>();
        for (User user : allUsers) {
            usersByRole.computeIfAbsent(user.getRole(), k -> new ArrayList<>()).add(user);
        }

        for (Role role : Role.values()) {
            List<User> roleUsers = usersByRole.getOrDefault(role, new ArrayList<>());
            reportBuilder.append("### ").append(role.name()).append(" Users (").append(roleUsers.size())
                    .append(")\n\n");

            if (!roleUsers.isEmpty()) {
                reportBuilder.append("| Name | Email | Password |\n");
                reportBuilder.append("|------|-------|----------|\n");

                for (User user : roleUsers) {
                    String password = role == Role.ADMIN ? "shiva"
                            : role == Role.HOSPITAL ? "hospital123" : role == Role.DOCTOR ? "doctor123" : "user123";
                    reportBuilder.append("| ").append(user.getName())
                            .append(" | `").append(user.getEmail())
                            .append("` | `").append(password).append("` |\n");
                }

                reportBuilder.append("\n");
            }
        }

        reportBuilder.append("---\n\n");

        // Section 4: Statistics
        reportBuilder.append("## 📊 DATABASE STATISTICS\n\n");
        reportBuilder.append("| Metric | Count |\n");
        reportBuilder.append("|--------|-------|\n");
        reportBuilder.append("| Total Hospitals | ").append(allClinics.size()).append(" |\n");
        reportBuilder.append("| Total Doctors | ").append(allDoctors.size()).append(" |\n");
        reportBuilder.append("| Total Users | ").append(allUsers.size()).append(" |\n");
        reportBuilder.append("| Hospital Owners | ")
                .append(usersByRole.getOrDefault(Role.HOSPITAL, new ArrayList<>()).size()).append(" |\n");
        reportBuilder.append("| Doctor Accounts | ")
                .append(usersByRole.getOrDefault(Role.DOCTOR, new ArrayList<>()).size()).append(" |\n");
        reportBuilder.append("| Admin Accounts | ")
                .append(usersByRole.getOrDefault(Role.ADMIN, new ArrayList<>()).size()).append(" |\n");
        reportBuilder.append("| Regular Users | ").append(usersByRole.getOrDefault(Role.USER, new ArrayList<>()).size())
                .append(" |\n");

        System.out.println("   ✅ Report generated successfully!\n");
    }

    private void saveReportToFile() {
        try {
            String filePath = "DATABASE_CREDENTIALS_COMPLETE.md";
            FileWriter writer = new FileWriter(filePath);
            writer.write(reportBuilder.toString());
            writer.close();

            System.out.println("   ✅ Report saved to: " + filePath + "\n");
        } catch (IOException e) {
            System.err.println("   ❌ Error saving report: " + e.getMessage());
        }
    }
}

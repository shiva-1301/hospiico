package com.hospitalfinder.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;

import com.hospitalfinder.backend.entity.User;
import com.hospitalfinder.backend.entity.Clinic;
import com.hospitalfinder.backend.entity.Doctor;
import com.hospitalfinder.backend.entity.Appointment;
import com.hospitalfinder.backend.entity.Review;
import com.hospitalfinder.backend.entity.Specialization;
import com.hospitalfinder.backend.entity.MedicalRecord;
import com.hospitalfinder.backend.entity.Role;
import com.hospitalfinder.backend.repository.UserRepository;

@Configuration
public class MongoDbConfig {

    @Bean
    public CommandLineRunner initializeMongoDb(MongoTemplate mongoTemplate, UserRepository userRepository) {
        return args -> {
            createIndexes(mongoTemplate);
            seedAdminUser(userRepository);
        };
    }

    private void createIndexes(MongoTemplate mongoTemplate) {
        try {
            // User indexes
            IndexOperations userIndexes = mongoTemplate.indexOps(User.class);
            userIndexes.ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());

            // Clinic indexes
            IndexOperations clinicIndexes = mongoTemplate.indexOps(Clinic.class);
            clinicIndexes.ensureIndex(new Index().on("city", Sort.Direction.ASC));
            clinicIndexes.ensureIndex(new Index().on("name", Sort.Direction.ASC));

            // Doctor indexes
            IndexOperations doctorIndexes = mongoTemplate.indexOps(Doctor.class);
            doctorIndexes.ensureIndex(new Index().on("clinicId", Sort.Direction.ASC));
            doctorIndexes.ensureIndex(new Index().on("specialization", Sort.Direction.ASC));

            // Appointment indexes
            IndexOperations appointmentIndexes = mongoTemplate.indexOps(Appointment.class);
            appointmentIndexes.ensureIndex(new Index().on("userId", Sort.Direction.ASC));
            appointmentIndexes.ensureIndex(new Index().on("clinicId", Sort.Direction.ASC));
            appointmentIndexes.ensureIndex(new Index().on("doctorId", Sort.Direction.ASC));

            // Review indexes
            IndexOperations reviewIndexes = mongoTemplate.indexOps(Review.class);
            reviewIndexes.ensureIndex(new Index().on("hospitalId", Sort.Direction.ASC));
            reviewIndexes.ensureIndex(new Index().on("doctorId", Sort.Direction.ASC));
            reviewIndexes.ensureIndex(new Index().on("userId", Sort.Direction.ASC));

            // Specialization indexes
            IndexOperations specIndexes = mongoTemplate.indexOps(Specialization.class);
            specIndexes.ensureIndex(new Index().on("specialization", Sort.Direction.ASC).unique());

            // Medical Record indexes
            IndexOperations medicalRecordIndexes = mongoTemplate.indexOps(MedicalRecord.class);
            medicalRecordIndexes.ensureIndex(new Index().on("userId", Sort.Direction.ASC));

            System.out.println("✓ MongoDB indexes created successfully!");
        } catch (Exception e) {
            System.out.println("⚠ Note: Some indexes may already exist. This is expected on subsequent runs.");
        }
    }

    private void seedAdminUser(UserRepository userRepository) {
        // Create/Update Shiva admin user
        User shivaAdmin = userRepository.findByEmail("shiva@gmail.com");
        if (shivaAdmin == null) {
            shivaAdmin = new User();
            shivaAdmin.setName("shiva");
            shivaAdmin.setEmail("shiva@gmail.com");
            shivaAdmin.setPhone("+1234567890");
            shivaAdmin.setAge(25);
            shivaAdmin.setGender("Male");
        }
        // Always update password and role to ensure correct credentials
        shivaAdmin.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdLa6ID/O"); // bcrypt for "shiva"
        shivaAdmin.setRole(Role.ADMIN);
        
        userRepository.save(shivaAdmin);
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("✓ ADMIN USER CREATED/UPDATED SUCCESSFULLY!");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("EMAIL:    shiva@gmail.com");
        System.out.println("PASSWORD: shiva");
        System.out.println("═══════════════════════════════════════════════════════════\n");
    }
}

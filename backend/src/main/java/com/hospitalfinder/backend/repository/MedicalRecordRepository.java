package com.hospitalfinder.backend.repository;

import com.hospitalfinder.backend.entity.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
    // Find records by user ID
    List<MedicalRecord> findByUserId(String userId);
}

package com.hospitalfinder.backend.service;

import com.hospitalfinder.backend.entity.MedicalRecord;
import com.hospitalfinder.backend.entity.User;
import com.hospitalfinder.backend.repository.MedicalRecordRepository;
import com.hospitalfinder.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public MedicalRecord uploadFile(MultipartFile file, String category, String userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        MedicalRecord medicalRecord = MedicalRecord.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .size(file.getSize())
                .category(category)
                .data(file.getBytes())
                .uploadDate(LocalDateTime.now())
                .userId(userId)
                .build();

        return medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getRecordsByUserId(String userId) {
        return medicalRecordRepository.findByUserId(userId);
    }

    public Optional<MedicalRecord> getRecordById(String id) {
        return medicalRecordRepository.findById(id);
    }

    @Transactional
    public void deleteRecord(String id) {
        medicalRecordRepository.deleteById(id);
    }
}

package com.hospitalfinder.backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hospitalfinder.backend.entity.Specialization;

public interface SpecializationRepository extends MongoRepository<Specialization, String> {
    boolean existsBySpecializationIgnoreCase(String specialization);
    Optional<Specialization> findBySpecializationIgnoreCase(String specialization);
}


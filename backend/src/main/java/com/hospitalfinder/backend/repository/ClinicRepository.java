package com.hospitalfinder.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hospitalfinder.backend.entity.Clinic;

public interface ClinicRepository extends MongoRepository<Clinic, String> {

    List<Clinic> findByCityIgnoreCase(String city);

    @Query("{ 'city': ?0, 'specializations.specialization': ?1 }")
    List<Clinic> findByCityAndSpecialization(@Param("city") String city,
            @Param("specialization") String specialization);

    @Query("{ 'specializations.specialization': ?0 }")
    List<Clinic> findBySpecialization(@Param("specialization") String specialization);

    boolean existsByNameIgnoreCaseAndAddressIgnoreCaseAndCityIgnoreCase(String name, String address, String city);

    boolean existsByName(String name);

    // Custom geospatial queries - will be implemented in service
    // For MongoDB geospatial queries, use 2dsphere index and Query annotations
    @Query("{ }")
    List<Clinic> findAllClinicsOrderedByDistance(Double latitude, Double longitude);
    
    @Query("{ }")
    List<Clinic> findNearestClinics(Double latitude, Double longitude);

    @Query("{ }")
    List<Clinic> findAllWithSpecializations();

    @Query(value = "{ }", fields = "{ 'city': 1 }")
    List<String> findAllDistinctCities();

    List<Clinic> findBySpecializationsIn(List<String> specializations);
}
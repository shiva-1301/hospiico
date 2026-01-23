package com.hospitalfinder.backend.repository;

import com.hospitalfinder.backend.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByHospitalId(String hospitalId);

    List<Review> findByDoctorId(String doctorId);

    List<Review> findByUserId(String userId);

    Review findByUserIdAndDoctorId(String userId, String doctorId);
}

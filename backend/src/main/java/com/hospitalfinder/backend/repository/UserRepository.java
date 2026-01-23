package com.hospitalfinder.backend.repository;

import com.hospitalfinder.backend.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);

    boolean existsByEmail(String email);
}

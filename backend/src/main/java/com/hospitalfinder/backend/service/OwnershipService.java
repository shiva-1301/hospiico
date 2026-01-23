package com.hospitalfinder.backend.service;

import org.springframework.stereotype.Service;

import com.hospitalfinder.backend.entity.Clinic;
import com.hospitalfinder.backend.entity.Role;
import com.hospitalfinder.backend.entity.User;
import com.hospitalfinder.backend.repository.ClinicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final ClinicRepository clinicRepository;

    /**
     * Check if a user owns a specific clinic
     */
    public boolean isOwner(String userId, String clinicId) {
        if (userId == null || clinicId == null) {
            return false;
        }

        return clinicRepository.findById(clinicId)
                .map(clinic -> userId.equals(clinic.getOwnerId()))
                .orElse(false);
    }

    /**
     * Check if user is admin or owns the clinic
     */
    public boolean isAdminOrOwner(User user, String clinicId) {
        if (user == null) {
            return false;
        }

        // Admins have full access
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Check ownership
        return isOwner(user.getId(), clinicId);
    }

    /**
     * Get the clinic owned by a user
     */
    public Clinic getOwnedClinic(String userId) {
        if (userId == null) {
            return null;
        }

        return clinicRepository.findAll().stream()
                .filter(clinic -> userId.equals(clinic.getOwnerId()))
                .findFirst()
                .orElse(null);
    }
}

package com.hospitalfinder.backend.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.hospitalfinder.backend.entity.Appointment;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    Collection<Appointment> findByUserId(String userId);
    Collection<Appointment> findByClinicId(String clinicId);
    List<Appointment> findByUserIdAndStatusIgnoreCase(String userId, String status);
    List<Appointment> findByClinicIdAndStatusIgnoreCase(String clinicId, String status);
    List<Appointment> findByDoctorId(String doctorId);
    boolean existsByUserIdAndClinicIdAndAppointmentTime(String userId, String clinicId, LocalDateTime appointmentTime);
    // check if a slot is already taken for a doctor
    boolean existsByDoctorIdAndAppointmentTime(String doctorId, LocalDateTime appointmentTime);

    // get all booked slots of a doctor for a date
    @Query("{ 'doctorId': ?0, 'appointmentTime': { $gte: ?1, $lt: ?2 } }")
    List<Appointment> findByDoctorAndDate(String doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}

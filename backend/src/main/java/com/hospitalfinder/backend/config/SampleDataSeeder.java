package com.hospitalfinder.backend.config;

import com.hospitalfinder.backend.entity.Clinic;
import com.hospitalfinder.backend.entity.Specialization;
import com.hospitalfinder.backend.repository.ClinicRepository;
import com.hospitalfinder.backend.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class SampleDataSeeder implements CommandLineRunner {

    private final SpecializationRepository specializationRepository;
    private final ClinicRepository clinicRepository;

    @Override
    public void run(String... args) {
        seedSpecializations();
        seedClinics();
    }

    private void seedSpecializations() {
        if (specializationRepository.count() == 0) {
            List<String> specs = Arrays.asList(
                    "Cardiology", "Neurology", "Orthopedics", "Pediatrics",
                    "Dermatology", "Gynecology", "Ophthalmology", "Dentistry",
                    "ENT", "General Medicine", "Psychiatry", "Radiology");

            for (String specName : specs) {
                Specialization spec = new Specialization();
                spec.setSpecialization(specName);
                specializationRepository.save(spec);
            }

            System.out.println("✅ Seeded " + specs.size() + " specializations");
        } else {
            System.out.println("✅ Specializations already exist - skipping");
        }
    }

    private void seedClinics() {
        if (clinicRepository.count() == 0) {
            // Sample clinic 1
            Clinic clinic1 = new Clinic();
            clinic1.setName("City General Hospital");
            clinic1.setCity("Hyderabad");
            clinic1.setAddress("Banjara Hills, Road No 12");
            clinic1.setLatitude(17.4239);
            clinic1.setLongitude(78.4738);
            clinic1.setPhone("+91-9876543210");
            clinic1.setWebsite("https://citygeneralhospital.com");
            clinic1.setTimings("24/7");
            clinic1.setRating(4.5);
            clinic1.setReviews(250);
            clinic1.setImageUrl("https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d");
            clinicRepository.save(clinic1);

            // Sample clinic 2
            Clinic clinic2 = new Clinic();
            clinic2.setName("Apollo Health Center");
            clinic2.setCity("Hyderabad");
            clinic2.setAddress("Jubilee Hills, Road No 36");
            clinic2.setLatitude(17.4326);
            clinic2.setLongitude(78.4071);
            clinic2.setPhone("+91-9876543211");
            clinic2.setWebsite("https://apollohealthcenter.com");
            clinic2.setTimings("8:00 AM - 10:00 PM");
            clinic2.setRating(4.7);
            clinic2.setReviews(420);
            clinic2.setImageUrl("https://images.unsplash.com/photo-1587351021759-3e566b6af7cc");
            clinicRepository.save(clinic2);

            // Sample clinic 3
            Clinic clinic3 = new Clinic();
            clinic3.setName("Care Hospitals");
            clinic3.setCity("Hyderabad");
            clinic3.setAddress("HITEC City, Madhapur");
            clinic3.setLatitude(17.4489);
            clinic3.setLongitude(78.3813);
            clinic3.setPhone("+91-9876543212");
            clinic3.setWebsite("https://carehospitals.com");
            clinic3.setTimings("24/7");
            clinic3.setRating(4.6);
            clinic3.setReviews(380);
            clinic3.setImageUrl("https://images.unsplash.com/photo-1516549655169-df83a0774514");
            clinicRepository.save(clinic3);

            System.out.println("✅ Seeded 3 sample clinics");
        } else {
            System.out.println("✅ Clinics already exist - skipping");
        }
    }
}

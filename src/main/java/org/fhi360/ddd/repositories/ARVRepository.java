package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.ARV;
import org.fhi360.ddd.domain.Facility;
import org.fhi360.ddd.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ARVRepository extends JpaRepository<ARV, Long> {

 Optional<ARV> findByPatientAndFacility(Patient patient, Facility facility);

}

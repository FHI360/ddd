package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    Facility findByName(String name);
}

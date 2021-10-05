package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.Outlet;
import org.fhi360.ddd.domain.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutletRepository extends JpaRepository<Outlet, Long> {
    Optional<Outlet> findByPinIgnoreCase(String pin);
    Optional<Outlet> findByEmail(String email);
    List<Outlet> findByFacility(Facility facility);

}

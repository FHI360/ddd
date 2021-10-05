package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.Regimen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegimenRepository extends JpaRepository<Regimen, Long> {
}

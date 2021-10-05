package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
}

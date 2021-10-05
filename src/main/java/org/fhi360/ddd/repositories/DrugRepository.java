package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugRepository extends  JpaRepository<Drug, Long>  {
}

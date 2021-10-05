package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.Outlet;
import org.fhi360.ddd.domain.Facility;
import org.fhi360.ddd.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByFacilityAndOutletAndArchived(Facility facility, Outlet communityPharmacy, Boolean archived);

    Optional<Patient> findByHospitalNumAndFacility(String hospitalNum, Facility facility);

    Optional<Patient> findByUniqueIdAndFacility(String hospitalNum, Facility facility);

    List<Patient> findByArchived(Boolean archived);

    List<Patient> findByFacilityAndArchived(Facility facility, Boolean archived);

    List<Patient> findByArchivedAndArvReceivedAndDateVisitBetween(Boolean archived, Boolean arvRecived, LocalDate dateArvRecivedStart,
                                                                        LocalDate dateArvRecivedEnd);

    List<Patient> findByArchivedAndArvReceivedAndDateVisitBetweenAndFacility(Boolean archived, Boolean arvRecived, LocalDate dateArvRecivedStart,
                                                                  LocalDate dateArvRecivedEnd,Facility facility);
}

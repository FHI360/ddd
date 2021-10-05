package org.fhi360.ddd.dto;

import lombok.Data;;
import org.fhi360.ddd.domain.Facility;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PatientDto {
    private Long id;
    private Facility facility;

    private String hospitalNum;

    private Long pharmacyId;
    private String uniqueId;

    private String surname;

    private String otherNames;

    private String gender;

    private String dateBirth;

    private String address;

    private String phone;

    private String uuid;

    private LocalDateTime lastModified;

    private Boolean archived = false;


    private String dateStarted;

    private String lastClinicStage;

    private double lastViralLoad;

    private String dateLastViralLoad;

    private String viralLoadDueDate;

    private String viralLoadType;

    private String dateLastRefill;

    private String dateNextRefill;

    private String dateLastClinic;

    private String dateNextClinic;

    private Long discontinued;

    private LocalDate dateDiscontinued;

    private String reasonDiscontinued;
    private String outLetName;
    private String dateVisit;
}

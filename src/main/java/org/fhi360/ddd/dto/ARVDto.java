package org.fhi360.ddd.dto;

import liquibase.exception.DatabaseException;
import lombok.Data;
import org.fhi360.ddd.domain.Patient;

import java.time.LocalDate;
import java.util.Date;

@Data
public class ARVDto {
    private Long id;
    private Patient patient;
    private Long facilityId;
    private String dateVisit;
    private String dateNextRefill;
    private Double bodyWeight;
    private Double height;
    private String bp;
    private Double bmi;
    private String bmiCategory;
    private String itp;
    private String tbTreatment;
    private String haveYouBeenCoughing;
    private String doYouHaveFever;
    private String areYouLosingWeight;
    private String areYouHavingSweet;
    private String doYouHaveSwellingNeck;
    private String tbReferred;
    private String eligibleIpt;
    private Long regimen1;
    private int duration1;
    private String prescribed1;
    private String dispensed1;
    private Long regimen2;
    private int duration2;
    private String prescribed2;
    private String dispensed2;
    private Long regimen3;
    private int duration3;
    private String prescribed3;
    private String dispensed3;
    private String regimen4;
    private int duration4;
    private String prescribed4;
    private String dispensed4;
    private String adverseIssue;
    private String adverseReport;
    private String dateNextClinic;
    private String viralLoadDeuDate;
    private String missedRefill;
    private String howMany;
    private String uuid;
    private String temperature;
    private Long discontinued;
    private String dateDiscontinued;
    private String reasonDiscontinued;
}

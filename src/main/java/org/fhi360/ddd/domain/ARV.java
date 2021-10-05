package org.fhi360.ddd.domain;

import lombok.Data;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@SQLDelete(sql = "update arv set archived = true, last_modified = current_timestamp where id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "archived = false")
public class ARV implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull
    private Patient patient;

    @ManyToOne
    @NotNull
    private Facility facility;

    @NotNull
    private LocalDate dateVisit;

    @NotNull
    private LocalDate dateNextRefill;

    private Double bodyWeight;

    private Double height;

    private String bp;

    private Double bmi;

    private String bmiCategory;

    @Column(name = "ipt")
    private Boolean itp;

    private Boolean coughing;

    private Boolean fever;

    private Boolean weightLoss;

    private Boolean sweating;

    private Boolean swellingNeck;

    private Boolean tbReferred;
    @Column(name = "itp_eligible")
    private Boolean iptEligible;

    @ManyToOne
    @JoinColumn(name = "regimen_1_id")
    private Regimen regimen1;

    @Column(name = "duration_1")
    private Integer duration1;


    @Column(name = "quantity_prescribed_1")
    private Double quantityPrescribed1;

    @Column(name = "quantity_dispensed_1")
    private Double quantityDispensed1;

    @ManyToOne
    @JoinColumn(name = "regimen_2_id")
    private Regimen regimen2;

    @Column(name = "duration_2")
    private Integer duration2;
    @Column(name = "quantity_prescribed_2")
    private Double quantityPrescribed2;

    @Column(name = "quantity_dispensed_2")
    private Double quantityDispensed2;

    @ManyToOne
    @JoinColumn(name = "regimen_3_id")
    private Regimen regimen3;

    @Column(name = "duration_3")
    private Integer duration3;

    @Column(name = "quantity_prescribed_3")
    private Double quantityPrescribed3;

    @Column(name = "quantity_dispensed_3")
    private Double quantityDispensed3;

    @ManyToOne
    @JoinColumn(name = "regimen_4_id")
    private Regimen regimen4;

    @Column(name = "duration_4")
    private Integer duration4;

    @Column(name = "quantity_prescribed_4")
    private Double quantityPrescribed4;

    @Column(name = "quantity_dispensed_4")
    private Double quantityDispensed4;

    @Column(name = "adverse_issue")
    private Boolean adverseIssue;
    @Column(name = "adverse_report")
    private String adverseReport;
    @Column(name = "date_next_clinic")
    private LocalDate dateNextClinic;

    @Column(name = "viral_load_due_date")
    private String viralLoadDueDate;


    @Column(name = "missed_refill")
    private Boolean missedRefill;

    @Column(name = "missed_refills")
    private Integer missedRefills;

    private String uuid;

    private LocalDateTime lastModified;

    private Boolean archived = false;


    @PrePersist
    public void prePersist() {
        uuid = UUID.randomUUID().toString();
        lastModified = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastModified = LocalDateTime.now();
    }
}

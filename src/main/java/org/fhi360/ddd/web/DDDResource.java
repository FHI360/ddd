package org.fhi360.ddd.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fhi360.ddd.domain.*;
import org.fhi360.ddd.dto.*;
import org.fhi360.ddd.repositories.*;
import org.fhi360.ddd.utils.EmailSender;
import org.fhi360.ddd.utils.ResourceException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Column;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/ddd/")
@RequiredArgsConstructor
@Slf4j
public class DDDResource {
    private final ARVRepository arvRepository;
    private final FacilityRepository facilityRepository;
    private final PatientRepository patientRepository;
    private final OutletRepository outletRepository;
    private final RegimenRepository regimenRepository;
    private final DeviceConfigRepository deviceConfigRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final EmailSender emailSender;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DrugRepository drugRepository;
    private final InventoryRepository inventoryRepository;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @PostMapping("mobile/pharmacy")
    private ResponseEntity<Response> saveOrUpdatePharmacy(@RequestBody Outlet pharmacy) {
        Response response = new Response();
        try {
            Optional<Outlet> communityPharmacy1 = this.outletRepository.findByEmail(pharmacy.getEmail());
            if (communityPharmacy1.isPresent()) {
                response.setMessage("Outlet with these credentials already exist");
                return ResponseEntity.ok(response);
            }
            long count = this.outletRepository.count();
            count++;
            String activationCode = "LIB00" + count;
            pharmacy.setPin(activationCode);
            Outlet communityPharmacy = this.outletRepository.save(pharmacy);
            String message = emailSender.activation(communityPharmacy.getName(), communityPharmacy.getUsername(), activationCode);
            emailSender.sendMail(communityPharmacy.getEmail(), "DDD Activation", message);
            response.setPharmacy(communityPharmacy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("mobile/sync/patient")
    private ResponseEntity<Response> syncPatient(@RequestBody List<Patient> patientDto) {
        Response response = new Response();
        patientDto.forEach(patient -> {
            Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(patient.getFacility().getId()));
            Optional<Patient> patient1 = this.patientRepository.findByHospitalNumAndFacility(patient.getHospitalNum(), facility);
            if (patient1.isPresent()) {
                patient = patient1.get();
                Outlet communityPharmacy = this.outletRepository.getOne(patient.getOutlet().getId());
                patient.setOutlet(communityPharmacy);
                this.patientRepository.save(patient);
            } else {
                Outlet communityPharmacy = this.outletRepository.getOne(patient.getPharmacyId());
                patient.setOutlet(communityPharmacy);
                this.patientRepository.save(patient);
            }

        });
        response.setMessage("patient success");
        return ResponseEntity.ok(response);
    }


    @PostMapping("mobile/save/patient")
    private ResponseEntity<Response> savePatient(@RequestBody Patient patient) {
        Response response = new Response();
        Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(patient.getFacility().getId()));
        Optional<Patient> patient1 = this.patientRepository.findByHospitalNumAndFacility(patient.getHospitalNum(), facility);
        if (patient1.isPresent()) {
            response.setMessage("Patient already exist");
            return ResponseEntity.ok(response);
        }

        Outlet communityPharmacy = this.outletRepository.getOne(patient.getPharmacyId());
        patient.setOutlet(communityPharmacy);
        this.patientRepository.save(patient);
        patient.setPharmacyId(patient.getPharmacyId());
        response.setPatient(patient);
        return ResponseEntity.ok(response);
    }


    @PostMapping("mobile/update/patient")
    private ResponseEntity<Response> updatePatient(@RequestBody Patient patient) {
        Response response = new Response();
        Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(patient.getFacility().getId()));
        Optional<Patient> patient1 = this.patientRepository.findByHospitalNumAndFacility(patient.getHospitalNum(), facility);
        if (patient1.isPresent()) {
            Patient patient2 = patient1.get();
            Outlet communityPharmacy = this.outletRepository.getOne(patient.getPharmacyId());
            patient2.setHospitalNum(patient.getUniqueId());
            patient2.setFacility(patient.getFacility());
            patient2.setUniqueId(patient.getUniqueId());
            patient2.setSurname(patient.getSurname());
            patient2.setOtherNames(patient.getOtherNames());
            patient2.setGender(patient.getGender());
            patient2.setDateBirth(patient.getDateBirth());
            patient2.setAddress(patient.getAddress());
            patient2.setPhone(patient.getPhone());
            patient2.setDateStarted(patient.getDateStarted());
            patient2.setLastClinicStage(patient.getLastClinicStage());
            patient2.setLastViralLoad(patient.getLastViralLoad());
            patient2.setDateLastViralLoad(patient.getDateLastViralLoad());
            patient2.setViralLoadDueDate(patient.getViralLoadDueDate());
            patient2.setViralLoadType(patient.getViralLoadType());
            patient2.setDateLastClinic(patient.getDateLastClinic());
            patient2.setDateNextClinic(patient.getDateNextClinic());
            patient2.setDateLastRefill(patient.getDateLastRefill());
            patient2.setDateNextRefill(patient.getDateNextRefill());
            patient2.setOutlet(communityPharmacy);
            this.patientRepository.save(patient2);
            patient2.setPharmacyId(patient2.getOutlet().getId());
            response.setPatient(patient2);
            return ResponseEntity.ok(response);
        }
        response.setMessage("Patient dose already exist");
        return ResponseEntity.ok(response);
    }

    //api/ddd/
    @PostMapping("mobile/sync/arv")
    private ResponseEntity<Response> syncARV(@RequestBody List<ARVDto> arvDtos) {
        Response response = new Response();
        arvDtos.forEach(arvs -> {
            Facility facility = this.facilityRepository.getOne(arvs.getFacilityId());
            Patient patient = this.patientRepository.getOne(arvs.getPatient().getId());
            Optional<ARV> checkIfExist = this.arvRepository.findByPatientAndFacility(patient, facility);
            if (checkIfExist.isPresent()) {
                ARV arv = checkIfExist.get();
                patient.setDateNextRefill(arvs.getDateNextRefill());
                patient.setArvReceived(Boolean.TRUE);
                patient.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
                this.patientRepository.save(patient);
                arv.setPatient(patient);
                arv.setFacility(facility);
                arv.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
                arv.setDateNextRefill(LocalDate.parse(arvs.getDateNextRefill(), formatter));
                arv.setBodyWeight(arvs.getBodyWeight());
                arv.setHeight(arvs.getHeight());
                arv.setBp(arvs.getBp());
                arv.setBmi(arvs.getBmi());
                arv.setBmiCategory(arvs.getBmiCategory());
                if (arvs.getId() != null) {
                    arv.setItp(Boolean.TRUE);
                } else {
                    arv.setItp(Boolean.FALSE);
                }
                if (arvs.getHaveYouBeenCoughing() != null) {
                    arv.setCoughing(Boolean.TRUE);
                } else {
                    arv.setCoughing(Boolean.FALSE);
                }

                if (arvs.getDoYouHaveFever() != null) {
                    arv.setFever(Boolean.TRUE);
                } else {
                    arv.setFever(Boolean.FALSE);
                }

                if (arvs.getAreYouLosingWeight() != null) {
                    arv.setWeightLoss(Boolean.TRUE);
                } else {
                    arv.setWeightLoss(Boolean.FALSE);
                }

                if (arvs.getAreYouHavingSweet() != null) {
                    arv.setSweating(Boolean.TRUE);
                } else {
                    arv.setSweating(Boolean.FALSE);
                }
                if (arvs.getDoYouHaveSwellingNeck() != null) {
                    arv.setSwellingNeck(Boolean.TRUE);
                } else {
                    arv.setSwellingNeck(Boolean.FALSE);
                }

                if (arvs.getTbReferred() != null) {
                    arv.setTbReferred(Boolean.TRUE);
                } else {
                    arv.setTbReferred(Boolean.FALSE);
                }
                if (arvs.getEligibleIpt() != null) {
                    arv.setIptEligible(Boolean.TRUE);
                } else {
                    arv.setIptEligible(Boolean.FALSE);
                }

                Regimen regimen1 = this.regimenRepository.getOne(arvs.getRegimen1());
                arv.setRegimen1(regimen1);
                arv.setDuration1(arvs.getDuration1());
                if (!StringUtils.isBlank(arvs.getDispensed1())) {
                    arv.setQuantityDispensed1(Double.valueOf(arvs.getDispensed1()));
                }

                arv.setQuantityPrescribed1(Double.valueOf(arvs.getPrescribed1()));


                Regimen regimen2 = this.regimenRepository.getOne(arvs.getRegimen2());
                arv.setRegimen2(regimen2);
                arv.setDuration2(arvs.getDuration2());
                if (!StringUtils.isBlank(arvs.getPrescribed2()) || !StringUtils.isBlank(arvs.getPrescribed2())) {
                    arv.setQuantityPrescribed2(Double.valueOf(arvs.getPrescribed2()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed2())) {
                    arv.setQuantityDispensed2(Double.valueOf(arvs.getDispensed2()));
                }
                Regimen regimen3 = this.regimenRepository.getOne(arvs.getRegimen3());
                arv.setRegimen3(regimen3);
                arv.setDuration3(arvs.getDuration3());
                if (!StringUtils.isBlank(arvs.getPrescribed3()) || !StringUtils.isBlank(arvs.getPrescribed3())) {
                    arv.setQuantityPrescribed3(Double.valueOf(arvs.getPrescribed3()));

                }
                if (!StringUtils.isBlank(arvs.getDispensed3())) {
                    arv.setQuantityDispensed3(Double.valueOf(arvs.getDispensed3()));
                }
                if (!StringUtils.isBlank(arvs.getRegimen4())) {
                    Regimen regimen4 = regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
                    arv.setRegimen4(regimen4);
                    arv.setDuration4(arvs.getDuration4());
                }

                if (!StringUtils.isBlank(arvs.getPrescribed4()) || !StringUtils.isBlank(arvs.getPrescribed4())) {
                    arv.setQuantityPrescribed4(Double.valueOf(arvs.getPrescribed4()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed4()) || !StringUtils.isBlank(arvs.getDispensed4())) {
                    arv.setQuantityPrescribed4(Double.valueOf(arvs.getDispensed4()));
                }
                if (arvs.getAdverseIssue() != null) {
                    arv.setAdverseIssue(Boolean.TRUE);
                } else {
                    arv.setAdverseIssue(Boolean.FALSE);
                }
                arv.setAdverseReport(arvs.getAdverseReport());
                arv.setDateNextClinic(LocalDate.parse(arvs.getDateNextClinic(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                arv.setViralLoadDueDate(arvs.getViralLoadDeuDate());
                if (arvs.getMissedRefill() != null) {
                    arv.setMissedRefill(Boolean.TRUE);
                } else {
                    arv.setMissedRefill(Boolean.FALSE);
                }
                if (!StringUtils.isBlank(arvs.getMissedRefill())) {
                    arv.setMissedRefills(1);
                } else {
                    arv.setMissedRefills(0);
                }
                this.arvRepository.save(arv);
            } else {
                ARV arv = new ARV();
                patient.setDateNextRefill(arvs.getDateNextRefill());
                patient.setArvReceived(Boolean.TRUE);
                patient.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
                this.patientRepository.save(patient);
                arv.setPatient(patient);
                arv.setFacility(facility);

                arv.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
                arv.setDateNextRefill(LocalDate.parse(arvs.getDateNextRefill(), formatter));
                arv.setBodyWeight(arvs.getBodyWeight());
                arv.setHeight(arvs.getHeight());
                arv.setBp(arvs.getBp());
                arv.setBmi(arvs.getBmi());
                arv.setBmiCategory(arvs.getBmiCategory());
                if (arvs.getId() != null) {
                    arv.setItp(Boolean.TRUE);
                } else {
                    arv.setItp(Boolean.FALSE);
                }
                if (arvs.getHaveYouBeenCoughing() != null) {
                    arv.setCoughing(Boolean.TRUE);
                } else {
                    arv.setCoughing(Boolean.FALSE);
                }

                if (arvs.getDoYouHaveFever() != null) {
                    arv.setFever(Boolean.TRUE);
                } else {
                    arv.setFever(Boolean.FALSE);
                }

                if (arvs.getAreYouLosingWeight() != null) {
                    arv.setWeightLoss(Boolean.TRUE);
                } else {
                    arv.setWeightLoss(Boolean.FALSE);
                }

                if (arvs.getAreYouHavingSweet() != null) {
                    arv.setSweating(Boolean.TRUE);
                } else {
                    arv.setSweating(Boolean.FALSE);
                }
                if (arvs.getDoYouHaveSwellingNeck() != null) {
                    arv.setSwellingNeck(Boolean.TRUE);
                } else {
                    arv.setSwellingNeck(Boolean.FALSE);
                }

                if (arvs.getTbReferred() != null) {
                    arv.setTbReferred(Boolean.TRUE);
                } else {
                    arv.setTbReferred(Boolean.FALSE);
                }
                if (arvs.getEligibleIpt() != null) {
                    arv.setIptEligible(Boolean.TRUE);
                } else {
                    arv.setIptEligible(Boolean.FALSE);
                }

                Regimen regimen1 = this.regimenRepository.getOne(arvs.getRegimen1());
                arv.setRegimen1(regimen1);
                arv.setDuration1(arvs.getDuration1());
                if (!StringUtils.isBlank(arvs.getDispensed1())) {
                    arv.setQuantityDispensed1(Double.valueOf(arvs.getDispensed1()));
                }

                arv.setQuantityPrescribed1(Double.valueOf(arvs.getPrescribed1()));


                Regimen regimen2 = this.regimenRepository.getOne(arvs.getRegimen2());
                arv.setRegimen2(regimen2);
                arv.setDuration2(arvs.getDuration2());
                if (!StringUtils.isBlank(arvs.getPrescribed2()) || !StringUtils.isBlank(arvs.getPrescribed2())) {
                    arv.setQuantityPrescribed2(Double.valueOf(arvs.getPrescribed2()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed2())) {
                    arv.setQuantityDispensed2(Double.valueOf(arvs.getDispensed2()));
                }
                Regimen regimen3 = this.regimenRepository.getOne(arvs.getRegimen3());
                arv.setRegimen3(regimen3);
                arv.setDuration3(arvs.getDuration3());
                if (!StringUtils.isBlank(arvs.getPrescribed3()) || !StringUtils.isBlank(arvs.getPrescribed3())) {
                    arv.setQuantityPrescribed3(Double.valueOf(arvs.getPrescribed3()));

                }
                if (!StringUtils.isBlank(arvs.getDispensed3())) {
                    arv.setQuantityDispensed3(Double.valueOf(arvs.getDispensed3()));
                }
                if (!StringUtils.isBlank(arvs.getRegimen4())) {
                    Regimen regimen4 = regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
                    arv.setRegimen4(regimen4);
                    arv.setDuration4(arvs.getDuration4());
                }

                if (!StringUtils.isBlank(arvs.getPrescribed4()) || !StringUtils.isBlank(arvs.getPrescribed4())) {
                    arv.setQuantityPrescribed4(Double.valueOf(arvs.getPrescribed4()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed4()) || !StringUtils.isBlank(arvs.getDispensed4())) {
                    arv.setQuantityPrescribed4(Double.valueOf(arvs.getDispensed4()));
                }
                if (arvs.getAdverseIssue() != null) {
                    arv.setAdverseIssue(Boolean.TRUE);
                } else {
                    arv.setAdverseIssue(Boolean.FALSE);
                }
                arv.setAdverseReport(arvs.getAdverseReport());
                arv.setDateNextClinic(LocalDate.parse(arvs.getDateNextClinic(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                arv.setViralLoadDueDate(arvs.getViralLoadDeuDate());
                if (arvs.getMissedRefill() != null) {
                    arv.setMissedRefill(Boolean.TRUE);
                } else {
                    arv.setMissedRefill(Boolean.FALSE);
                }
                if (!StringUtils.isBlank(arvs.getMissedRefill())) {
                    arv.setMissedRefills(1);
                } else {
                    arv.setMissedRefills(0);
                }

                this.arvRepository.save(arv);
            }
        });
        response.setMessage("ARV success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("mobile/save/arv")
    private ResponseEntity<Response> saveARYV(@RequestBody ARVDto arvs) {
        Response response = new Response();
        ARV arv = new ARV();
        Patient patient = this.patientRepository.getOne(arvs.getPatient().getId());
        patient.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
        patient.setDateNextRefill(arvs.getDateNextRefill());
        patient.setArvReceived(Boolean.TRUE);
        this.patientRepository.save(patient);
        arv.setPatient(patient);
        Facility facility = this.facilityRepository.getOne(arvs.getFacilityId());
        arv.setFacility(facility);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        arv.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
        arv.setDateNextRefill(LocalDate.parse(arvs.getDateNextRefill(), formatter));
        arv.setBodyWeight(arvs.getBodyWeight());
        arv.setHeight(arvs.getHeight());
        arv.setBp(arvs.getBp());
        arv.setBmi(arvs.getBmi());
        arv.setBmiCategory(arvs.getBmiCategory());
        if (arvs.getId() != null) {
            arv.setItp(Boolean.TRUE);
        } else {
            arv.setItp(Boolean.FALSE);
        }
        if (arvs.getHaveYouBeenCoughing() != null) {
            arv.setCoughing(Boolean.TRUE);
        } else {
            arv.setCoughing(Boolean.FALSE);
        }

        if (arvs.getDoYouHaveFever() != null) {
            arv.setFever(Boolean.TRUE);
        } else {
            arv.setFever(Boolean.FALSE);
        }

        if (arvs.getAreYouLosingWeight() != null) {
            arv.setWeightLoss(Boolean.TRUE);
        } else {
            arv.setWeightLoss(Boolean.FALSE);
        }

        if (arvs.getAreYouHavingSweet() != null) {
            arv.setSweating(Boolean.TRUE);
        } else {
            arv.setSweating(Boolean.FALSE);
        }
        if (arvs.getDoYouHaveSwellingNeck() != null) {
            arv.setSwellingNeck(Boolean.TRUE);
        } else {
            arv.setSwellingNeck(Boolean.FALSE);
        }

        if (arvs.getTbReferred() != null) {
            arv.setTbReferred(Boolean.TRUE);
        } else {
            arv.setTbReferred(Boolean.FALSE);
        }
        if (arvs.getEligibleIpt() != null) {
            arv.setIptEligible(Boolean.TRUE);
        } else {
            arv.setIptEligible(Boolean.FALSE);
        }

        Regimen regimen1 = this.regimenRepository.getOne(arvs.getRegimen1());
        arv.setRegimen1(regimen1);
        arv.setDuration1(arvs.getDuration1());
        if (!StringUtils.isBlank(arvs.getDispensed1())) {
            arv.setQuantityDispensed1(Double.valueOf(arvs.getDispensed1()));
        }

        arv.setQuantityPrescribed1(Double.valueOf(arvs.getPrescribed1()));


        Regimen regimen2 = this.regimenRepository.getOne(arvs.getRegimen2());
        arv.setRegimen2(regimen2);
        arv.setDuration2(arvs.getDuration2());
        if (!StringUtils.isBlank(arvs.getPrescribed2()) || !StringUtils.isBlank(arvs.getPrescribed2())) {
            arv.setQuantityPrescribed2(Double.valueOf(arvs.getPrescribed2()));
        }
        if (!StringUtils.isBlank(arvs.getDispensed2())) {
            arv.setQuantityDispensed2(Double.valueOf(arvs.getDispensed2()));
        }
        Regimen regimen3 = this.regimenRepository.getOne(arvs.getRegimen3());
        arv.setRegimen3(regimen3);
        arv.setDuration3(arvs.getDuration3());
        if (!StringUtils.isBlank(arvs.getPrescribed3()) || !StringUtils.isBlank(arvs.getPrescribed3())) {
            arv.setQuantityPrescribed3(Double.valueOf(arvs.getPrescribed3()));

        }
        if (!StringUtils.isBlank(arvs.getDispensed3())) {
            arv.setQuantityDispensed3(Double.valueOf(arvs.getDispensed3()));
        }
        if (!StringUtils.isBlank(arvs.getRegimen4())) {
            Regimen regimen4 = regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
            arv.setRegimen4(regimen4);
            arv.setDuration4(arvs.getDuration4());
        }

        if (!StringUtils.isBlank(arvs.getPrescribed4()) || !StringUtils.isBlank(arvs.getPrescribed4())) {
            arv.setQuantityPrescribed4(Double.valueOf(arvs.getPrescribed4()));
        }
        if (!StringUtils.isBlank(arvs.getDispensed4()) || !StringUtils.isBlank(arvs.getDispensed4())) {
            arv.setQuantityPrescribed4(Double.valueOf(arvs.getDispensed4()));
        }
        if (arvs.getAdverseIssue() != null) {
            arv.setAdverseIssue(Boolean.TRUE);
        } else {
            arv.setAdverseIssue(Boolean.FALSE);
        }
        arv.setAdverseReport(arvs.getAdverseReport());
        arv.setDateNextClinic(LocalDate.parse(arvs.getDateNextClinic(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        arv.setViralLoadDueDate(arvs.getViralLoadDeuDate());
        if (arvs.getMissedRefill() != null) {
            arv.setMissedRefill(Boolean.TRUE);
        } else {
            arv.setMissedRefill(Boolean.FALSE);
        }
        if (!StringUtils.isBlank(arvs.getMissedRefill())) {
            arv.setMissedRefills(1);
        } else {
            arv.setMissedRefills(0);
        }
        this.arvRepository.save(arv);
        response.setMessage("ARV success");
        return ResponseEntity.ok(response);
    }


    @GetMapping("mobile/patient/{deviceId}/{pin}/{accountUserName}/{accountPassword}")
    private ResponseEntity<Map<String, Object>> activateOutLet(@PathVariable("deviceId") String deviceId,
                                                               @PathVariable("pin") String pin,
                                                               @PathVariable("accountUserName") String accountUserName,
                                                               @PathVariable("accountPassword") String accountPassword) {
        Outlet communityPharmacy = null;
        try {
            communityPharmacy = this.outletRepository.findByPinIgnoreCase(pin).orElseThrow(() -> new ResourceException("Activation pin does not  exist:   " + pin));
        } catch (ResourceException resourceException) {
            resourceException.printStackTrace();
        }
        Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(Objects.requireNonNull(communityPharmacy).getFacility().getId()));
        DeviceConfig deviceConfig1 = this.deviceConfigRepository.findByDeviceId(deviceId);
        if (deviceConfig1 == null) {
            DeviceConfig deviceconfig = new DeviceConfig();
            deviceconfig.setDeviceId(deviceId);
            deviceconfig.setUsername(accountUserName);
            deviceconfig.setPassword(accountPassword);
            deviceconfig.setFacility(facility);
            this.deviceConfigRepository.save(deviceconfig);
        }
        List<PatientDto> patientList = new ArrayList<>();
        List<Patient> patients = this.patientRepository.findByFacilityAndOutletAndArchived(facility, communityPharmacy, Boolean.FALSE);
        patients.forEach(patient -> {
            PatientDto patient1 = new PatientDto();
            patient1.setId(patient.getId());
            patient1.setHospitalNum(patient.getUniqueId());
            patient1.setFacility(patient.getFacility());
            patient1.setUniqueId(patient.getUniqueId());
            patient1.setSurname(patient.getSurname());
            patient1.setOtherNames(patient.getOtherNames());
            patient1.setGender(patient.getGender());
            patient1.setDateBirth(String.valueOf(patient.getDateBirth()));
            patient1.setAddress(patient.getAddress());
            patient1.setPhone(patient.getPhone());
            patient1.setDateStarted(patient.getDateStarted());
            patient1.setLastClinicStage(patient.getLastClinicStage());
            patient1.setLastViralLoad(patient.getLastViralLoad());
            patient1.setDateLastViralLoad(patient.getDateLastViralLoad());
            patient1.setViralLoadDueDate(patient.getViralLoadDueDate());
            patient1.setViralLoadType(patient.getViralLoadType());
            patient1.setDateLastClinic(patient.getDateLastClinic());
            patient1.setDateNextClinic(patient.getDateNextClinic());
            patient1.setDateLastRefill(patient.getDateLastRefill());
            patient1.setDateNextRefill(patient.getDateNextRefill());
            patient1.setPharmacyId(patient.getOutlet().getId());
            patientList.add(patient1);

        });
        List<Regimen> regimenList = new ArrayList<>();
        List<Inventory> regimenList1 = this.inventoryRepository.findByOutlet(communityPharmacy);
        regimenList1.forEach(inventory -> {
            Regimen regimen = new Regimen();
            regimen.setId(inventory.getRegimen().getId());
            regimen.setRegimenTypeId(inventory.getRegimen().getRegimenTypeId());
            regimen.setName(inventory.getRegimen().getName());
            regimenList.add(regimen);
        });

        Map<String, Object> obj = new HashMap<>();
        obj.put("facility", facility);
        obj.put("regimens", regimenList);
        obj.put("patients", patientList);
        return ResponseEntity.ok(obj);
    }

    @GetMapping("mobile/pharmacy/{pin}")
    private ResponseEntity<Map<String, Object>> activatePharmacyAccount(@PathVariable("pin") String pin) {
        Outlet communityPharmacy = null;
        try {
            communityPharmacy = this.outletRepository.findByPinIgnoreCase(pin).orElseThrow(() -> new ResourceException("Activation pin does not  exist:   " + pin));
        } catch (ResourceException resourceException) {
            resourceException.printStackTrace();
        }
        // Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(Objects.requireNonNull(communityPharmacy).getFacility().getId()));
        Map<String, Object> obj = new HashMap<>();
        obj.put("pharmacy", communityPharmacy);
        return ResponseEntity.ok(obj);
    }

    //api/ddd/mobile/patient/{deviceId}/{pin}/{accountUserName}/{accountPassword}
    @GetMapping("mobile/facility/{deviceId}/{facilityId}/{accountUserName}/{accountPassword}")
    private ResponseEntity<Map<String, Object>> activateFacility(@PathVariable("deviceId") String deviceId,
                                                                 @PathVariable("facilityId") Long facilityId,
                                                                 @PathVariable("accountUserName") String accountUserName,
                                                                 @PathVariable("accountPassword") String accountPassword) {

        Facility facility = null;
        try {
            facility = this.facilityRepository.findById(facilityId).orElseThrow(() -> new ResourceException("Invalid facility code " + facilityId));
        } catch (ResourceException e) {
            e.printStackTrace();
        }
        DeviceConfig deviceConfig = this.deviceConfigRepository.findByDeviceId(deviceId);
        if (deviceConfig == null) {
            DeviceConfig deviceconfig = new DeviceConfig();
            deviceconfig.setDeviceId(deviceId);
            deviceconfig.setUsername(accountUserName);
            deviceconfig.setPassword(accountPassword);
            deviceconfig.setFacility(facility);
            this.deviceConfigRepository.save(deviceconfig);
        }

        List<Regimen> regimenList = this.regimenRepository.findAll();
        List<District> districtList = this.districtRepository.findByState(Objects.requireNonNull(facility).getState());
        List<DistrictDto> outPutDistrict = new ArrayList<>();
        districtList.forEach(district -> {
            DistrictDto dto = new DistrictDto();
            dto.setName(district.getName());
            dto.setId(district.getId());
            dto.setStateId(district.getState().getId());
            outPutDistrict.add(dto);
        });

        FacilityDto facilityDto = new FacilityDto();
        facilityDto.setId(facility.getId());
        facilityDto.setName(facility.getName());
        facilityDto.setStateId(facility.getState().getId());
        facilityDto.setDistrictId(facility.getDistrict().getId());
        List<Outlet> communityPharmacies = this.outletRepository.findAll();
        Map<String, Object> obj = new HashMap<>();
        obj.put("facility", facilityDto);
        obj.put("state", Objects.requireNonNull(facility).getState());
        obj.put("district", outPutDistrict);
        obj.put("regimens", regimenList);
        obj.put("communityPharmacies", communityPharmacies);
        return ResponseEntity.ok(obj);
    }

    @GetMapping("mobile/login/{username}/{password}")
    private ResponseEntity<Response> login(@PathVariable String username, @PathVariable String password) {
        Response response = new Response();
        Optional<User> user = this.userRepository.findByUsernameAndPasswordIgnoreCase(username, password);
        if (user.isPresent()) {
            User user1 = user.get();
            UserDto result = new UserDto();
            result.setUsername(user1.getUsername());
            result.setPassword(user1.getPassword());
            result.setRole(user1.getRole());
            Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(user1.getFacility().getId()));
            result.setFacilityName(facility.getName());
            result.setFacilityId(facility.getId());
            response.setUser(result);
            return ResponseEntity.ok(response);
        } else {

            response.setMessage("User not found");
        }
        return ResponseEntity.ok(response);
    }

    public int getAge(String dateBirth) {
        LocalDate currentDate = LocalDate.parse(dateBirth);
        LocalDate pdate = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), currentDate.getDayOfMonth());
        LocalDate now = LocalDate.now();
        Period diff = Period.between(pdate, now);
        return diff.getYears();
    }

    @PostMapping("mobile/discontinue/{dateDiscontinue}/{reasonDiscontinued}/{id}")
    public ResponseEntity<Response> discontinued(@PathVariable("dateDiscontinue") String dateDiscontinue, @PathVariable("reasonDiscontinued") String reasonDiscontinued, @PathVariable("id") Long id) {
        Response response = new Response();
        this.updatePatient(LocalDate.parse(dateDiscontinue, DateTimeFormatter.ofPattern("yyyy-MM-dd")), reasonDiscontinued, id);
        response.setMessage("Success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("get-pharmacy/{facilityId}")
    public ResponseEntity<List<Outlet>> getPharmacy(@PathVariable("facilityId") Long facilityId) {
        Facility facility = this.facilityRepository.getOne(facilityId);
        List<Outlet> communityPharmacies = this.outletRepository.findByFacility(facility);
        return ResponseEntity.ok(communityPharmacies);
    }

    public void updatePatient(LocalDate dateDiscontinue, String reasonDiscontinued, Long id) {
        jdbcTemplate.update("update patient set date_discontinued = ?, reason_discontinued = ? , discontinued = ? where id = ?", dateDiscontinue, reasonDiscontinued, 1, id);
    }

    @PostMapping("mobile/save/drug")
    private ResponseEntity<Response> saveDrug(@RequestBody DrugDto drug) {
        Response response = new Response();
        Drug drug1 = new Drug();
        Regimen regimen = this.regimenRepository.getOne(drug.getRegimeId());
        drug1.setRegimen(regimen);
        drug1.setName(drug.getDrugName());
        drug1.setBasicUnit(drug.getBasicUnit());
        this.drugRepository.save(drug1);
        response.setMessage("Drug success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("mobile/save/inventory")
    private ResponseEntity<Response> saveInventory(@RequestBody InventoryDto inventory) {
        Response response = new Response();
        Inventory issuedDrug = new Inventory();
        issuedDrug.setBatchNumber(inventory.getBatchNumber());
        Regimen regimen = this.regimenRepository.getOne(inventory.getRegimenId());
        issuedDrug.setRegimen(regimen);
        Optional<Outlet> outlet = this.outletRepository.findByPinIgnoreCase(inventory.getPinCode());
        if (outlet.isPresent()) {
            Outlet outlet1 = outlet.get();
            issuedDrug.setOutlet(outlet1);
        }

        issuedDrug.setExpireDate(inventory.getExpireDate());
        issuedDrug.setQuantity(inventory.getQuantity());
        this.inventoryRepository.save(issuedDrug);
        response.setMessage("Inventory success");
        return ResponseEntity.ok(response);
    }


    @GetMapping("mobile-patient/all")
    private ResponseEntity<Map<String, Object>> getPatient() {
        List<PatientDto> patientList = new ArrayList<>();
        List<Patient> patients = this.patientRepository.findByArchived(Boolean.FALSE);
        patients.forEach(patient -> {
            PatientDto patient1 = new PatientDto();
            patient1.setId(patient.getId());
            patient1.setHospitalNum(patient.getUniqueId());
            patient1.setFacility(patient.getFacility());
            patient1.setUniqueId(patient.getUniqueId());
            patient1.setSurname(patient.getSurname());
            patient1.setOtherNames(patient.getOtherNames());
            patient1.setGender(patient.getGender());
            patient1.setDateBirth(String.valueOf(patient.getDateBirth()));
            patient1.setAddress(patient.getAddress());
            patient1.setPhone(patient.getPhone());
            patient1.setDateStarted(patient.getDateStarted());
            patient1.setLastClinicStage(patient.getLastClinicStage());
            patient1.setLastViralLoad(patient.getLastViralLoad());
            patient1.setDateLastViralLoad(patient.getDateLastViralLoad());
            patient1.setViralLoadDueDate(patient.getViralLoadDueDate());
            patient1.setViralLoadType(patient.getViralLoadType());
            patient1.setDateLastClinic(patient.getDateLastClinic());
            patient1.setDateNextClinic(patient.getDateNextClinic());
            patient1.setDateLastRefill(patient.getDateLastRefill());
            patient1.setDateNextRefill(patient.getDateNextRefill());
            patient1.setPharmacyId(patient.getOutlet().getId());
            patient1.setOutLetName(patient.getOutlet().getName());
            patientList.add(patient1);
        });
        Map<String, Object> obj = new HashMap<>();
        obj.put("patients", patientList);
        return ResponseEntity.ok(obj);
    }


    @DeleteMapping("mobile-patient-id/delete/{id}")
    private void deletePatient(@PathVariable("id") Long id) throws ResourceException {
        Optional<Patient> patient = this.patientRepository.findById(id);
        patient.map(patient1 -> {
            patient1.setArchived(Boolean.TRUE);
            this.patientRepository.save(patient1);
            return ResponseEntity.ok();
        }).orElseThrow(() -> new ResourceException("ID does not  exist:   " + id));
    }


    @DeleteMapping("mobile-patient/delete")
    private void removePatientByUniqueIdAndFacility(@RequestParam(value = "hospitalNum") String hospitalNum, @RequestParam(value = "facilityId") Long facilityId) throws ResourceException {
        Facility facility = this.facilityRepository.getOne(facilityId);
        Optional<Patient> patient = this.patientRepository.findByUniqueIdAndFacility(hospitalNum, facility);
        patient.map(patient1 -> {
            patient1.setArchived(Boolean.TRUE);
            this.patientRepository.save(patient1);
            return ResponseEntity.ok();
        }).orElseThrow(() -> new ResourceException("HospitalNum &  FacilityId  does not  exist:   " + hospitalNum + "   " + facilityId));
    }


    @GetMapping("mobile-patient/arv/date-rage/{start}/{end}")
    private ResponseEntity<Map<String, Object>> patientByDateRange(@PathVariable("start") String start, @PathVariable("end") String end) {
        List<PatientDto> patientList = new ArrayList<>();
        List<Patient> patients = this.patientRepository.findByArchivedAndArvReceivedAndDateVisitBetween(Boolean.FALSE, Boolean.TRUE, LocalDate.parse(start), LocalDate.parse(end));
        patients.forEach(patient -> {
            PatientDto patient1 = new PatientDto();
            patient1.setId(patient.getId());
            patient1.setHospitalNum(patient.getUniqueId());
            patient1.setFacility(patient.getFacility());
            patient1.setUniqueId(patient.getUniqueId());
            patient1.setSurname(patient.getSurname());
            patient1.setOtherNames(patient.getOtherNames());
            patient1.setGender(patient.getGender());
            patient1.setDateBirth(String.valueOf(patient.getDateBirth()));
            patient1.setAddress(patient.getAddress());
            patient1.setPhone(patient.getPhone());
            patient1.setDateStarted(patient.getDateStarted());
            patient1.setLastClinicStage(patient.getLastClinicStage());
            patient1.setLastViralLoad(patient.getLastViralLoad());
            patient1.setDateLastViralLoad(patient.getDateLastViralLoad());
            patient1.setViralLoadDueDate(patient.getViralLoadDueDate());
            patient1.setViralLoadType(patient.getViralLoadType());
            patient1.setDateLastClinic(patient.getDateLastClinic());
            patient1.setDateNextClinic(patient.getDateNextClinic());
            patient1.setDateLastRefill(patient.getDateLastRefill());
            patient1.setDateNextRefill(patient.getDateNextRefill());
            patient1.setPharmacyId(patient.getOutlet().getId());
            patient1.setOutLetName(patient.getOutlet().getName());
            patient1.setDateVisit(String.valueOf(patient.getDateVisit()));
            patientList.add(patient1);
        });
        Map<String, Object> obj = new HashMap<>();
        obj.put("patients", patientList);
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/mobile-patient/facility/{facilityId}")
    private ResponseEntity<Map<String, Object>> patientByFacility(@PathVariable("facilityId") Long facilityId) {
        List<PatientDto2> patientList = new ArrayList<>();
        Facility facility = this.facilityRepository.getOne(facilityId);
        List<Patient> patients = this.patientRepository.findByFacilityAndArchived(facility, Boolean.FALSE);
        patients.forEach(patient -> {
            PatientDto2 patient1 = new PatientDto2();
            patient1.setId(patient.getId());
            patient1.setHospitalNum(patient.getUniqueId());
            Facility2 facility2 = new Facility2();
            facility2.setId(patient.getFacility().getId());
            facility2.setName(patient.getFacility().getName());
            patient1.setFacility(facility2);
            patient1.setUniqueId(patient.getUniqueId());
            patient1.setSurname(patient.getSurname());
            patient1.setOtherNames(patient.getOtherNames());
            patient1.setGender(patient.getGender());
            patient1.setDateBirth(String.valueOf(patient.getDateBirth()));
            patient1.setAddress(patient.getAddress());
            patient1.setPhone(patient.getPhone());
            patient1.setDateStarted(patient.getDateStarted());
            patient1.setLastClinicStage(patient.getLastClinicStage());
            patient1.setLastViralLoad(patient.getLastViralLoad());
            patient1.setDateLastViralLoad(patient.getDateLastViralLoad());
            patient1.setViralLoadDueDate(patient.getViralLoadDueDate());
            patient1.setViralLoadType(patient.getViralLoadType());
            patient1.setDateLastClinic(patient.getDateLastClinic());
            patient1.setDateNextClinic(patient.getDateNextClinic());
            patient1.setDateLastRefill(patient.getDateLastRefill());
            patient1.setDateNextRefill(patient.getDateNextRefill());
            patient1.setPharmacyId(patient.getOutlet().getId());
            patient1.setOutLetName(patient.getOutlet().getName());
            patient1.setDateVisit(String.valueOf(patient.getDateVisit()));
            patientList.add(patient1);
        });
        Map<String, Object> obj = new HashMap<>();
        obj.put("patients", patientList);
        return ResponseEntity.ok(obj);
    }


    @GetMapping("mobile-patient/arv/date-rage/{start}/{end}/{facilityId}")
    private ResponseEntity<Map<String, Object>> patientByDateRange1(@PathVariable("start") String start,
                                                                    @PathVariable("end") String end,@PathVariable("facilityId") Long facilityId) {
      Facility facility = this.facilityRepository.getOne(facilityId);
        List<PatientDto> patientList = new ArrayList<>();
        List<Patient> patients = this.patientRepository.findByArchivedAndArvReceivedAndDateVisitBetweenAndFacility(Boolean.FALSE, Boolean.TRUE, LocalDate.parse(start), LocalDate.parse(end),facility);
        patients.forEach(patient -> {
            PatientDto patient1 = new PatientDto();
            patient1.setId(patient.getId());
            patient1.setHospitalNum(patient.getUniqueId());
            Facility facility1 = new Facility();
            facility1.setId(patient.getFacility().getId());
            facility1.setName(patient.getFacility().getName());
            patient1.setFacility(facility1);
            patient1.setUniqueId(patient.getUniqueId());
            patient1.setSurname(patient.getSurname());
            patient1.setOtherNames(patient.getOtherNames());
            patient1.setGender(patient.getGender());
            patient1.setDateBirth(String.valueOf(patient.getDateBirth()));
            patient1.setAddress(patient.getAddress());
            patient1.setPhone(patient.getPhone());
            patient1.setDateStarted(patient.getDateStarted());
            patient1.setLastClinicStage(patient.getLastClinicStage());
            patient1.setLastViralLoad(patient.getLastViralLoad());
            patient1.setDateLastViralLoad(patient.getDateLastViralLoad());
            patient1.setViralLoadDueDate(patient.getViralLoadDueDate());
            patient1.setViralLoadType(patient.getViralLoadType());
            patient1.setDateLastClinic(patient.getDateLastClinic());
            patient1.setDateNextClinic(patient.getDateNextClinic());
            patient1.setDateLastRefill(patient.getDateLastRefill());
            patient1.setDateNextRefill(patient.getDateNextRefill());
            patient1.setPharmacyId(patient.getOutlet().getId());
            patient1.setOutLetName(patient.getOutlet().getName());
            patient1.setDateVisit(String.valueOf(patient.getDateVisit()));
            patientList.add(patient1);
        });
        Map<String, Object> obj = new HashMap<>();
        obj.put("patients", patientList);
        return ResponseEntity.ok(obj);
    }
}

package org.fhi360.ddd.dto;

import lombok.Data;
import org.fhi360.ddd.domain.Facility;
import org.fhi360.ddd.domain.Outlet;
import org.fhi360.ddd.domain.Patient;
@Data
public class Response2 {
    private String message;
    private Outlet pharmacy;
    private PatientDto patient;
    private UserDto user;
    private Facility facility;
}

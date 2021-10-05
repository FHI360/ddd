package org.fhi360.ddd.dto;

import lombok.Data;

@Data
public class FacilityDto {
    private Long id;
    private String name;
    private Long stateId;
    private Long districtId;
}

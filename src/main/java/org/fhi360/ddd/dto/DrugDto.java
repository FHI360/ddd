package org.fhi360.ddd.dto;

import lombok.Data;

@Data
public class DrugDto {
    private String drugName;
    private String basicUnit;
    private Long regimeId;
}

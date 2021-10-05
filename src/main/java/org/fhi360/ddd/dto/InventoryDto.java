package org.fhi360.ddd.dto;

import lombok.Data;

@Data
public class InventoryDto {
    private String pinCode;
    private Long regimenId;
    private String batchNumber;
    private String expireDate;
    private Long quantity;

}

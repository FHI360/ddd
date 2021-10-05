package org.fhi360.ddd.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String role;
    private String facilityName;
    private Long facilityId;

}

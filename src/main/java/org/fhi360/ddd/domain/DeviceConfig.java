/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fhi360.ddd.domain;

import lombok.Data;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Data
public class DeviceConfig implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "facility_id")
    @ManyToOne
    private Facility facility;

    @NotNull
    private String deviceId;

    private String username;

    private String password;

    @Override
    public boolean isNew() {
        return id == null;
    }
}

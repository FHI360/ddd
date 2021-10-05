package org.fhi360.ddd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@ToString(of = "name")
public class Facility implements Serializable, Persistable<Long> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Long id;
    @NotNull
    @ManyToOne
    @JsonIgnore
    private State state;
    @NotNull
    @ManyToOne
    @JsonIgnore
    private District district;
    @Basic(optional = false)
    private String name;
    @JsonIgnore
    private String facilityType;
    @JsonIgnore
    private String address1;
    @JsonIgnore
    private String address2;
    @JsonIgnore
    private String phone1;
    @JsonIgnore
    private String phone2;

    //   private String emailSender;
    @JsonIgnore
    private Boolean active;
    @JsonIgnore
    private String datimId;

    @Override
    @JsonIgnore
    public boolean isNew() {
        return id == null;
    }
}

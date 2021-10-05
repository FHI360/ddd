package org.fhi360.ddd.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@EqualsAndHashCode(of = "id")
public class Country implements Serializable, Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String code;

    @Override
    public boolean isNew() {
        return id == null;
    }
}

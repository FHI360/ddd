package org.fhi360.ddd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A State.
 */
@Entity
@Data
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public final class State implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @JoinColumn(name = "country_id")
    @ManyToOne
    @JsonIgnore
    private Country country;

    @Override
    @JsonIgnore
    public boolean isNew() {
        return id == null;
    }
}

package org.fhi360.ddd.domain;


import lombok.Data;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Data
@Entity
@SQLDelete(sql = "update outlet set archived = true, last_modified = current_timestamp where id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "archived = false")
public class Outlet implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private Facility facility;

    private String address;

    private String phone;

    @NotNull
    private String email;

    private String type;

    private String pin;

    private String uuid;

    private String username;

    private LocalDateTime lastModified;

    private Boolean archived = false;

    @PrePersist
    public void prePersist() {
        uuid = UUID.randomUUID().toString();
        lastModified = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastModified = LocalDateTime.now();
    }


}

package org.fhi360.ddd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Data
@SQLDelete(sql = "update inventory set archived = true where id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "archived = false")
public class Inventory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Outlet outlet;
    @ManyToOne
    private Regimen regimen;
    private String batchNumber;
    private String expireDate;
    private Long quantity;
    private boolean archived = false;


}

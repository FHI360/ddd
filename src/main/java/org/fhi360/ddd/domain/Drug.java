package org.fhi360.ddd.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@SQLDelete(sql = "update drug set archived = true where id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "archived = false")
public class Drug implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String basicUnit;
    private boolean archived = false;
    @ManyToOne
    private Regimen regimen;
}

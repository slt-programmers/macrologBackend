package slt.database.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Weight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Integer id;

    private Double weight = 0.0;
    private Date day = Date.valueOf(LocalDate.now());

    @Column(length = 65535, columnDefinition="TEXT")
    private String remark;

    @Column(name = "user_id")
    private Integer userId;

}
